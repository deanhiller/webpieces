package org.webpieces.nio.impl.cm.basic;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.util.HostWithPort;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.RecordingDataListener;
import org.webpieces.nio.impl.util.ChannelSessionImpl;
import org.webpieces.util.exceptions.NioClosedChannelException;
import org.webpieces.util.exceptions.NioException;

/**
 * @author Dean Hiller
 */
public abstract class BasChannelImpl
	extends RegisterableChannelImpl
	implements Channel {

	private static final Logger apiLog = LoggerFactory.getLogger(Channel.class);
	private static final Logger log = LoggerFactory.getLogger(BasChannelImpl.class);

    private ChannelSession session = new ChannelSessionImpl();
	private BufferPool pool;
	private KeyProcessor router;
	private DataListener dataListener;
	private Object writeLock = new Object();
	
    private long waitingBytesCounter = 0;
	private ConcurrentLinkedQueue<WriteInfo> dataToBeWritten = new ConcurrentLinkedQueue<WriteInfo>();
	private int maxBytesWaitingSize = 500_000;
	
	private boolean inDelayedWriteMode;
	private boolean isRecording;

	//for clients only
	protected SocketAddress isConnectingTo;

	protected ChannelState channelState;

	private boolean isRemoteEndInitiateClose;
	private Integer maxUnackedBytes;
	private Integer readingThreshold;

	private AtomicInteger unackedBytes = new AtomicInteger(0);
	private AtomicReference<BackflowState1> backflowState = new AtomicReference<>(BackflowState1.REGISTERED);

	public BasChannelImpl(String id, SelectorManager2 selMgr, KeyProcessor router, BufferPool pool, BackpressureConfig config) {
		super(id, selMgr);
		this.pool = pool;
		this.isRecording = false;
		this.router = router;
		this.maxUnackedBytes = config.getMaxBytes();
		this.readingThreshold = config.getStartReadingThreshold();
	}
	
	/* (non-Javadoc)
	 * @see biz.xsoftware.nio.RegisterableChannelImpl#getRealChannel()
	 */
	//public abstract SelectableChannel getRealChannel();

	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.RegisterableChannel#isBlocking()
	 */
	public abstract boolean isBlocking();

	public abstract int readImpl(ByteBuffer b);
	protected abstract int writeImpl(ByteBuffer b);

	/**
	 * Because people re-use SocketAddress which is dangerous as java reads DNS once and then
	 * if ips are changed, everyone is screwed basically.  Instead, we now take
	 * an oject with host or ip and a port (pure data object)
	 */
	@Override
	public XFuture<Void> connect(HostWithPort addr, DataListener listener) {
		InetSocketAddress socketAddress;
		if(addr.getHostOrIpAddress() == null) {
			socketAddress = new InetSocketAddress(addr.getPort());
		} else {
			socketAddress = new InetSocketAddress(addr.getHostOrIpAddress(), addr.getPort());
		}
		return connectReuse(socketAddress, listener);
	}

	/**
	 * @deprecated Use connect(HostWithPort, DataListener) or connect(IpWithPort, DataListener) instead
	 */
	@Deprecated
	@Override
	public XFuture<Void> connect(SocketAddress addr, DataListener listener) {
		//delete later
		return connectReuse(addr, listener);
	}

	public XFuture<Void> connectReuse(SocketAddress addr, DataListener listener) {
		this.dataListener = listener;
		
		if(isRecording) 
			dataListener = new RecordingDataListener("singleThreaded-", listener);
		
		XFuture<Channel> future = connectImpl(addr);
		return future.thenCompose(v -> {
			channelState = ChannelState.CONNECTED;
			return registerForReads(dataListener);
		});
	}
	
    protected abstract XFuture<Channel> connectImpl(SocketAddress addr);

	private void unqueueAndFailWritesThenClose(CloseRunnable action) {
    	List<XFuture<Void>> promises;
    	synchronized(this) { //put here for emphasis that we are synchronizing here but not below
			promises = failAllWritesInQueue();
    	}
    	
		//TODO: This should really be inlined now.  It's a remnant of an old design since close didn't
		//work well outside the selector thread previously
		action.runDelayedAction();
		
		//we used to do this to put the close on the selector thread but if writes are held up it won't work
    	//registerForWritesOrClose();
    	
    	//notify clients outside the synchronization block!!!
		for(XFuture<Void> promise : promises) {
    		log.info("WRITES outstanding while close was called, notifying client through his failure method of the exception");
    		//we only incur the cost of Throwable.fillInStackTrace() if we will use this exception
    		//(it's called in the Throwable constructor) so we don't do this on every close channel
        	NioClosedChannelException closeExc = new NioClosedChannelException(this+"There are "+promises.size()
        			+" writes that are not complete yet(you called write but "
        			+ "they did not call success back to the client).");
        	promise.completeExceptionally(closeExc);
		}
    }

	@Override
	public XFuture<Void> write(ByteBuffer b) {
		if(b.remaining() == 0)
			throw new IllegalArgumentException(this+"buffer has no data");
		else if(!selMgr.isRunning())
			throw new IllegalStateException(this+"ChannelManager must be running and is stopped");		
		else if(channelState == ChannelState.CLOSED) {
			//TODO: dhiller I feel like we should give the client a few writes before we start throwing if
			//isRemoteEndInitiateClose = true since this is a likely scenario OR even better, allow 2 seconds
			//to pass before throwing as by then, the client has been notified and they really should have shut
			//the fuck up
			if(isRemoteEndInitiateClose)
				throw new NioClosedChannelException(this+" Client cannot write after the remote end closed the socket");
			else
				throw new NioClosedChannelException(this+"Your Application cannot write after YOUR Application closed the socket");
		} else if(channelState != ChannelState.CONNECTED) {
			throw new NioException(this+"The Channel is not connected yet");
		}
		
		if(apiLog.isTraceEnabled())
			apiLog.trace(this+"Basic.write");
		
		return writeSynchronized(b)
				.thenApply(v -> {
					pool.releaseBuffer(b);
					return null;
				});
	}
	
	private XFuture<Void> writeSynchronized(ByteBuffer b) {
		
		//I feel like there is a bit too much in this sync block BUT this is also a complex problem and
		//easy to get wrong.
		XFuture<Void> future = new XFuture<Void>();
		synchronized (writeLock ) {
			if(!inDelayedWriteMode) {
				int totalToWriteOut = b.remaining();
				int written = writeImpl(b);
				if(written != totalToWriteOut) {
					if(b.remaining() + written != totalToWriteOut)
						throw new IllegalStateException(this+"Something went wrong.  b.remaining()="+b.remaining()+" written="+written+" total="+totalToWriteOut);

					registerForWrites();
					inDelayedWriteMode = true;
				} else {
					if(log.isTraceEnabled())
						log.trace(this+" wrote bytes on client thread");
					return XFuture.completedFuture(null);
				}
			}

			if(log.isTraceEnabled())
				log.trace(this+"sent write to queue");
			WriteInfo holder = new WriteInfo(b, future);
			dataToBeWritten.add(holder);

			waitingBytesCounter += b.remaining();
			if(waitingBytesCounter > maxBytesWaitingSize) {
				//log.warn("You have "+waitingBytesCounter+" bytes waiting to be written");
			}
		}

        return future;
	}

	//synchronized with writeAll as both try to go through every element in the queue
	//while most of the time there will be no contention(only on the close do we hit this)
	private synchronized List<XFuture<Void>> failAllWritesInQueue() {
		List<XFuture<Void>> copy = new ArrayList<>();
		while(!dataToBeWritten.isEmpty()) {
			WriteInfo runnable = dataToBeWritten.remove();
			ByteBuffer buffer = runnable.getBuffer();
			buffer.position(buffer.limit()); //mark buffer read before releasing it
			pool.releaseBuffer(buffer);
			copy.add(runnable.getPromise());
		}
		
		waitingBytesCounter = 0;
		return copy;
	}

	private void registerForWrites() {
        if(log.isTraceEnabled())
			log.trace(this+"registering channel for write msg. size="+dataToBeWritten.size());
        selMgr.registerSelectableChannel(this, SelectionKey.OP_WRITE, null, () -> true);
	}
       
    /**
     * This method is reading from the queue and writing out to the socket buffers that
     * did not get written out when client called write.
     *
     */
	 void writeAll(SelectionKey key) {
		List<XFuture<Void>> finishedPromises = new ArrayList<>();
		synchronized(writeLock) {
	        if(dataToBeWritten.isEmpty())
	        	throw new IllegalStateException(this+"bug, I am not sure this is possible..it shouldn't be...look into. channel="+this);
	
	        while(!dataToBeWritten.isEmpty()) {
	            WriteInfo writer = dataToBeWritten.peek();
	            ByteBuffer buffer = writer.getBuffer();
	            int initialSize = buffer.remaining();
	    		int wroteOut = this.writeImpl(buffer);
	            if(buffer.hasRemaining()) {
					if(buffer.remaining() + wroteOut != initialSize)
						throw new IllegalStateException(this+"Something went wrong.  b.remaining()="+buffer.remaining()+" written="+wroteOut+" total="+initialSize+" this="+this);
					
	                if(log.isTraceEnabled())
						log.trace(this+"Did not write all data out");
	                int leftOverSize = buffer.remaining();
	                int writtenOut = initialSize - leftOverSize;
	                waitingBytesCounter -= writtenOut;
	                break;
	            }
	            
	            //if it finished, remove the item from the queue.  It
	            //does not need to be run again.
	            dataToBeWritten.poll();

	            waitingBytesCounter -= initialSize;
	            finishedPromises.add(writer.getPromise());
	        }
	        
	        //we are registered for writes with ANY size queue
	        if(dataToBeWritten.isEmpty() && inDelayedWriteMode) {
	        	inDelayedWriteMode = false;
	        	if(log.isTraceEnabled())
					log.trace(this+"unregister writes");
	            router.unregisterSelectableChannel(this, SelectionKey.OP_WRITE, key);
	        }
		}
		
        //MAKE SURE to notify clients outside of synchronization block so no deadlocks with their locks
        for(XFuture<Void> promise : finishedPromises) {
        	promise.complete(null);
        }
    }
		
    public XFuture<Void> bind(SocketAddress addr) {
        if(!(addr instanceof InetSocketAddress))
            throw new IllegalArgumentException(this+"Can only bind to InetSocketAddress addressses");
        if(apiLog.isTraceEnabled())
			apiLog.trace(this+"Basic.bind called addr="+addr);
        
        try {
			bindImpl(addr);
			return XFuture.completedFuture(null);
		} catch (IOException e) {
			throw new NioException(e);
		}
    }
    
    private void bindImpl(SocketAddress addr) throws IOException {
        try {
            bindImpl2(addr);
        } catch(Error e) {
            //NOTE:  jdk was throwing Error instead of BindException.  We fix
            //this and throw BindException which is the logical choice!!
            //We are crossing our fingers hoping there are not other SocketExceptions
            //from things other than address already in use!!!
            if(e.getCause() instanceof SocketException) {
                BindException exc = new BindException(e.getMessage());
                exc.initCause(e.getCause());
                throw exc;
            }
            throw e;
        }        
    }
    
    /**
     * 
     * @param addr
     * @throws IOException
     */
    protected abstract void bindImpl2(SocketAddress addr) throws IOException;
    
    XFuture<Void> registerForReads(DataListener l) {
    	this.dataListener = l;
    	return registerForReads(() -> true);
    }
    
	public XFuture<Void> registerForReads(Supplier<Boolean> shouldRegister) {
		if(dataListener == null)
			throw new IllegalArgumentException(this+"listener cannot be null");
		else if(channelState != ChannelState.CONNECTED) {
			throw new IllegalStateException(this+"Must call one of the connect methods first(ie. connect THEN register for reads)");
		} else if(isClosed())
			throw new IllegalStateException(this+"Channel is closed");

		if(apiLog.isTraceEnabled())
			apiLog.trace(this+"Basic.registerForReads called");
		
        try {
			return selMgr.registerChannelForRead(this, dataListener, shouldRegister);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}
	
	public XFuture<Channel> unregisterForReads() {
		if(apiLog.isTraceEnabled())
			apiLog.trace(this+"Basic.unregisterForReads called");		
		try {
			return selMgr.unregisterChannelForRead(this).thenApply(v -> this);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}	
           
	protected void setConnectingTo(SocketAddress addr) {
		this.isConnectingTo = addr;
	}

	protected void setClosed(boolean isServerClosing) {
		isRemoteEndInitiateClose = isServerClosing;
		channelState = ChannelState.CLOSED;
	}
    
    @Override
    public XFuture<Void> close() {
        //To prevent the following exception, in the readImpl method, we
        //check if the socket is already closed, and if it is we don't read
        //and just return -1 to indicate socket closed.
    	XFuture<Void> future = new XFuture<>();
    	try {
    		if(apiLog.isTraceEnabled())
				apiLog.trace(this+"Basic.close called");
    		
    		if(!isOpen()) {
	        	future.complete(null);
	        	return future;
	        }
	        
	        setClosed(false);
	        CloseRunnable runnable = new CloseRunnable(this, future);
	        unqueueAndFailWritesThenClose(runnable);
        } catch(Exception e) {
            log.error(this+"Exception closing channel", e);
            future.completeExceptionally(e);
        }
    	return future;
    }
    
    protected abstract boolean isOpen();
    
    public void serverClosed() throws IOException {
    	setClosed(true);
    	closeImpl();
    }
    protected abstract void closeImpl() throws IOException;

    public ChannelSession getSession() {
    	return session;
    }

	public boolean isOverMaxUnacked(int unackedBytes) {
		return unackedBytes >= maxUnackedBytes;
	}

	public boolean isUnderThreshold(int unackedBytes) {
		if(unackedBytes <= readingThreshold)
			return true;
		return false;
	}

	public Integer getMaxUnacked() {
		return maxUnackedBytes;
	}
	
	public int getReadThreshold() {
		return readingThreshold;
	}

	public AtomicInteger getUnackedBytes() {
    	return unackedBytes;
	}

	public AtomicReference<BackflowState1> getCompareSetBackflowState() {
    	return backflowState;
	}
}
