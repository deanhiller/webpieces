package org.webpieces.nio.impl.cm.basic;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;
import org.webpieces.nio.api.exceptions.NioException;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.RecordingDataListener;
import org.webpieces.nio.impl.util.ChannelSessionImpl;
import org.webpieces.ssl.api.ConnectionState;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

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
	private int unackedBytes;
	private int maxUnackedBytes;
	private Integer readingThreshold;

	public BasChannelImpl(IdObject id, SelectorManager2 selMgr, KeyProcessor router, BufferPool pool, BackpressureConfig config) {
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
   
	@Override
	public CompletableFuture<Channel> connect(SocketAddress addr, DataListener listener) {
		this.dataListener = listener;
		
		if(isRecording) 
			dataListener = new RecordingDataListener("singleThreaded-", listener);
		
		CompletableFuture<Channel> future = connectImpl(addr);
		return future.thenCompose(c -> {
			channelState = ChannelState.CONNECTED;
			return registerForReads(dataListener);
		});
	}
	
    protected abstract CompletableFuture<Channel> connectImpl(SocketAddress addr);

	private void unqueueAndFailWritesThenClose(CloseRunnable action) {
    	List<CompletableFuture<Void>> promises;
    	synchronized(this) { //put here for emphasis that we are synchronizing here but not below
			promises = failAllWritesInQueue();
    	}
    	
		//TODO: This should really be inlined now.  It's a remnant of an old design since close didn't
		//work well outside the selector thread previously
		action.runDelayedAction();
		
		//we used to do this to put the close on the selector thread but if writes are held up it won't work
    	//registerForWritesOrClose();
    	
    	//notify clients outside the synchronization block!!!
		for(CompletableFuture<Void> promise : promises) {
    		log.info("WRITES outstanding while close was called, notifying client through his failure method of the exception");
    		//we only incur the cost of Throwable.fillInStackTrace() if we will use this exception
    		//(it's called in the Throwable constructor) so we don't do this on every close channel
        	NioClosedChannelException closeExc = new NioClosedChannelException("There are "+promises.size()
        			+" writes that are not complete yet(you called write but "
        			+ "they did not call success back to the client).");
        	promise.completeExceptionally(closeExc);
		}
    }

	@Override
	public CompletableFuture<Channel> write(ByteBuffer b) {
		if(b.remaining() == 0)
			throw new IllegalArgumentException("buffer has no data");
		else if(!selMgr.isRunning())
			throw new IllegalStateException(this+"ChannelManager must be running and is stopped");		
		else if(channelState == ChannelState.CLOSED) {
			if(isRemoteEndInitiateClose)
				throw new NioClosedChannelException(this+"Client cannot write after the remote end closed the socket");
			else
				throw new NioClosedChannelException(this+"Your Application cannot write after YOUR Application closed the socket");
		} else if(channelState != ChannelState.CONNECTED) {
			throw new NioException("The Channel is not connected yet");
		}
		
		apiLog.trace(()->this+"Basic.write");
		
		return writeSynchronized(b)
				.thenApply(v -> {
					pool.releaseBuffer(b);
					return this;
				});
	}
	
	private CompletableFuture<Void> writeSynchronized(ByteBuffer b) {
		
		//I feel like there is a bit too much in this sync block BUT this is also a complex problem and
		//easy to get wrong.
		CompletableFuture<Void> future = new CompletableFuture<Void>();
		synchronized (writeLock ) {
			if(!inDelayedWriteMode) {
				int totalToWriteOut = b.remaining();
				int written = writeImpl(b);
				if(written != totalToWriteOut) {
					if(b.remaining() + written != totalToWriteOut)
						throw new IllegalStateException("Something went wrong.  b.remaining()="+b.remaining()+" written="+written+" total="+totalToWriteOut);

					registerForWrites();
					inDelayedWriteMode = true;
				} else {
					log.trace(()->this+" wrote bytes on client thread");
					return CompletableFuture.completedFuture(null);
				}
			}

			log.trace(()->this+"sent write to queue");
			WriteInfo holder = new WriteInfo(b, future);
			dataToBeWritten.add(holder);

			waitingBytesCounter += b.remaining();
			if(waitingBytesCounter > maxBytesWaitingSize) {
				log.warn("You have "+waitingBytesCounter+" bytes waiting to be written");
			}
		}

        return future;
	}

	//synchronized with writeAll as both try to go through every element in the queue
	//while most of the time there will be no contention(only on the close do we hit this)
	private synchronized List<CompletableFuture<Void>> failAllWritesInQueue() {
		List<CompletableFuture<Void>> copy = new ArrayList<>();
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
        log.trace(()->this+"registering channel for write msg. size="+dataToBeWritten.size());
        selMgr.registerSelectableChannel(this, SelectionKey.OP_WRITE, null);
	}
       
    /**
     * This method is reading from the queue and writing out to the socket buffers that
     * did not get written out when client called write.
     *
     */
	 void writeAll() {
		List<CompletableFuture<Void>> finishedPromises = new ArrayList<>();
		synchronized(writeLock) {
	        if(dataToBeWritten.isEmpty())
	        	throw new IllegalStateException("bug, I am not sure this is possible..it shouldn't be...look into");
	
	        while(!dataToBeWritten.isEmpty()) {
	            WriteInfo writer = dataToBeWritten.peek();
	            ByteBuffer buffer = writer.getBuffer();
	            int initialSize = buffer.remaining();
	    		int wroteOut = this.writeImpl(buffer);
	            if(buffer.hasRemaining()) {
					if(buffer.remaining() + wroteOut != initialSize)
						throw new IllegalStateException("Something went wrong.  b.remaining()="+buffer.remaining()+" written="+wroteOut+" total="+initialSize);
					
	                log.trace(()->this+"Did not write all data out");
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
	        	log.trace(()->this+"unregister writes");
	            router.unregisterSelectableChannel(this, SelectionKey.OP_WRITE);
	        }
		}
		
        //MAKE SURE to notify clients outside of synchronization block so no deadlocks with their locks
        for(CompletableFuture<Void> promise : finishedPromises) {
        	promise.complete(null);
        }
    }
		
    public CompletableFuture<Void> bind(SocketAddress addr) {
        if(!(addr instanceof InetSocketAddress))
            throw new IllegalArgumentException(this+"Can only bind to InetSocketAddress addressses");
        apiLog.trace(()->this+"Basic.bind called addr="+addr);
        
        try {
			bindImpl(addr);
			return CompletableFuture.completedFuture(null);
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
    
    CompletableFuture<Channel> registerForReads(DataListener l) {
    	this.dataListener = l;
    	return registerForReads();
    }
    
	public CompletableFuture<Channel> registerForReads() {
		if(dataListener == null)
			throw new IllegalArgumentException(this+"listener cannot be null");
		else if(channelState != ChannelState.CONNECTED) {
			throw new IllegalStateException(this+"Must call one of the connect methods first(ie. connect THEN register for reads)");
		} else if(isClosed())
			throw new IllegalStateException("Channel is closed");

		apiLog.trace(()->this+"Basic.registerForReads called");
		
        try {
			return selMgr.registerChannelForRead(this, dataListener).thenApply(v -> this);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}
	
	public CompletableFuture<Channel> unregisterForReads() {
		apiLog.trace(()->this+"Basic.unregisterForReads called");		
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

	protected void setClosed(boolean b, boolean isServerClosing) {
		isRemoteEndInitiateClose = isServerClosing;
		channelState = ChannelState.CLOSED;
	}
    
    @Override
    public CompletableFuture<Channel> close() {
        //To prevent the following exception, in the readImpl method, we
        //check if the socket is already closed, and if it is we don't read
        //and just return -1 to indicate socket closed.
    	CompletableFuture<Channel> future = new CompletableFuture<>();
    	try {
    		apiLog.trace(()->this+"Basic.close called");
    		
    		if(!isOpen()) {
	        	future.complete(this);
	        	return future;
	        }
	        
	        setClosed(true, false);
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
    	setClosed(true, true);
    	closeImpl();
    }
    protected abstract void closeImpl() throws IOException;

    public ChannelSession getSession() {
    	return session;
    }

	public void addUnackedByteCount(int bytes) {
		unackedBytes += bytes;
	}

	public boolean isOverMaxUnacked() {
		return unackedBytes >= maxUnackedBytes;
	}

	public boolean isUnderThreshold() {
		if(unackedBytes <= readingThreshold)
			return true;
		return false;
	}
	
}
