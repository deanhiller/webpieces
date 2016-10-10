package org.webpieces.nio.impl.cm.basic;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;
import org.webpieces.nio.api.exceptions.NioException;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.RecordingDataListener;
import org.webpieces.nio.impl.util.ChannelSessionImpl;

/**
 * @author Dean Hiller
 */
public abstract class BasChannelImpl
	extends RegisterableChannelImpl
	implements Channel {

	private static final Logger apiLog = LoggerFactory.getLogger(Channel.class);
	private static final Logger log = LoggerFactory.getLogger(BasChannelImpl.class);

    private ChannelSession session = new ChannelSessionImpl();
    private long waitingBytesCounter = 0;
	private ConcurrentLinkedQueue<WriteInfo> dataToBeWritten = new ConcurrentLinkedQueue<WriteInfo>();
	private boolean isConnecting = false;
	private boolean isClosed = false;
	private boolean doNotAllowWrites;
	private int writeTimeoutMs = 5_000;
	private int maxBytesWaitingSize = 500_000; //0.5 megabyte before telling client to backpressure the channel
	private AtomicBoolean applyingBackpressure = new AtomicBoolean(false);
	private boolean isRegisterdForReads;
	private BufferPool pool;
	private DataListener dataListener;
	private Object writeLock = new Object();
	private boolean inDelayedWriteMode;
	private boolean isRecording;
	
	public BasChannelImpl(IdObject id, SelectorManager2 selMgr, BufferPool pool) {
		super(id, selMgr);
		this.pool = pool;
		this.isRecording = false;
	}
	
	/* (non-Javadoc)
	 * @see biz.xsoftware.nio.RegisterableChannelImpl#getRealChannel()
	 */
	public abstract SelectableChannel getRealChannel();

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
		return future.thenApply(c -> {
			registerForReads(dataListener);
			return c;
		});
	}
	
    protected abstract CompletableFuture<Channel> connectImpl(SocketAddress addr);

	private void unqueueAndFailWritesThenClose(CloseRunnable action) {
    	List<CompletableFuture<Channel>> promises;
    	synchronized(this) { //put here for emphasis that we are synchronizing here but not below
			promises = failAllWritesInQueue();
    	}
    	
		//TODO: This should really be inlined now.  It's a remnant of an old design since close didn't
		//work well outside the selector thread previously
		action.runDelayedAction();
		
		//we used to do this to put the close on the selector thread but if writes are held up it won't work
    	//registerForWritesOrClose();
    	
    	//notify clients outside the synchronization block!!!
		for(CompletableFuture<Channel> promise : promises) {
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
		else if(!getSelectorManager().isRunning())
			throw new IllegalStateException(this+"ChannelManager must be running and is stopped");		
		else if(isClosed)
			throw new NioClosedChannelException(this+"Client cannot write after the client closed the socket");
		else if(doNotAllowWrites)
			throw new IllegalStateException("This channel is in a failed state.  "
					+ "failure functions were called so look for exceptions from them");
		
		apiLog.trace(()->this+"Basic.write");
		
		CompletableFuture<Channel> future = new CompletableFuture<Channel>();
		
		boolean wroteAllData = writeSynchronized(b, future);

		if(wroteAllData) {
			//since we didn't switch and were not in this mode, complete the action outside sync block
			pool.releaseBuffer(b);
			future.complete(this);
			log.trace(()->this+" wrote bytes on client thread");			
		} else {
			log.trace(()->this+"sent write to queue");
		}
		
		return future;
	}
	
	private boolean writeSynchronized(ByteBuffer b, CompletableFuture<Channel> future) {
		
		//I feel like there is a bit too much in this sync block BUT this is also an extremely complex problem and 
		//VERY VERY VERY easy to get wrong.  The calls I don't really like in here are registerForWrites()
		//and dataListener.applyBackPressure but both are pretty complex AND it is very important to not have
		//race conditions between 
		//1. turning on and off write registration with the selector
		//2. turning on and off back pressure with the client
		//These operations need to get to the selector or client IN ORDER and not have race conditions to work
		//The corresponding code to worry about is writeAll method which reads from the waitingWriters
		synchronized (writeLock ) {
			if(!inDelayedWriteMode) {
				int totalToWriteOut = b.remaining();
				int written = writeImpl(b);
				if(written != totalToWriteOut) {
					if(b.remaining() + written != totalToWriteOut)
						throw new IllegalStateException("Something went wrong.  b.remaining()="+b.remaining()+" written="+written+" total="+totalToWriteOut);
					
					registerForWrites();
					inDelayedWriteMode = true;
				} else
					return true;
			}

			WriteInfo holder = new WriteInfo(b, future);
			dataToBeWritten.add(holder);
			
			boolean needToApplyBackpressure = false;
			waitingBytesCounter += b.remaining();
			if(waitingBytesCounter > maxBytesWaitingSize) {
				needToApplyBackpressure = true;
			}
			
			boolean changedValue = applyingBackpressure.compareAndSet(false, needToApplyBackpressure);
			if(needToApplyBackpressure && changedValue) {
				//we only fire when the value of applyingBackpressure changes
				//Also, this is a real PITA since we must do this in the sync block and I don't like calling
				//customers in a sync block though thankfully most won't use the single threaded channelmanager.
				//The reason is that if this is outside sync block, just before executor.execute({Runnable with call to
				//applyBackPressure}) is called (run from the writer thread that is), the channelmanager thread
				//can then call releaseBackPressure and that can beat applyBackPressure up the stack(and has)
				//This results in the client permanently enabling pressure since it is the last call....when
				//we need releaseBackPressure to always be after apply.
				dataListener.applyBackPressure(this);
			}
		}
		
        return false;
	}

	//synchronized with writeAll as both try to go through every element in the queue
	//while most of the time there will be no contention(only on the close do we hit this)
	private synchronized List<CompletableFuture<Channel>> failAllWritesInQueue() {
		doNotAllowWrites = true;
		List<CompletableFuture<Channel>> copy = new ArrayList<>();
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
        getSelectorManager().registerSelectableChannel(this, SelectionKey.OP_WRITE, null);
	}
       
    /**
     * This method is reading from the queue and writing out to the socket buffers that
     * did not get written out when client called write.
     *
     */
	 void writeAll() {
		List<CompletableFuture<Channel>> finishedPromises = new ArrayList<>();
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
	            //release bytebuffer back to pool
	            pool.releaseBuffer(writer.getBuffer());
	            waitingBytesCounter -= initialSize;
	            finishedPromises.add(writer.getPromise());
	        }
	        
	        //we are only applying backpressure when queue is too large
	        boolean applyPressure = !dataToBeWritten.isEmpty();
	        boolean changedValue = applyingBackpressure.compareAndSet(true, applyPressure);
	        if(!applyPressure && changedValue) {
	        	//we only fire when the value of applyingBackpressure changes
	        	dataListener.releaseBackPressure(this);
	        }
	        
	        //we are registered for writes with ANY size queue
	        if(dataToBeWritten.isEmpty() && inDelayedWriteMode) {
	        	inDelayedWriteMode = false;
	        	log.trace(()->this+"unregister writes");
	            Helper.unregisterSelectableChannel(this, SelectionKey.OP_WRITE);
	        }
		}
		
        //MAKE SURE to notify clients outside of synchronization block so no deadlocks with their locks
        for(CompletableFuture<Channel> promise : finishedPromises) {
        	promise.complete(this);
        }
    }
		
    public void bind(SocketAddress addr) {
        if(!(addr instanceof InetSocketAddress))
            throw new IllegalArgumentException(this+"Can only bind to InetSocketAddress addressses");
        apiLog.trace(()->this+"Basic.bind called addr="+addr);
        
        try {
			bindImpl(addr);
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
    
    void registerForReads(DataListener l) {
    	this.dataListener = l;
    	registerForReads();
    }
    
	public CompletableFuture<Channel> registerForReads() {
		if(dataListener == null)
			throw new IllegalArgumentException(this+"listener cannot be null");
		else if(!isConnecting && !isConnected()) {
			throw new IllegalStateException(this+"Must call one of the connect methods first(ie. connect THEN register for reads)");
		} else if(isClosed())
			throw new IllegalStateException("Channel is closed");

		apiLog.trace(()->this+"Basic.registerForReads called");
		
        try {
			return getSelectorManager().registerChannelForRead(this, dataListener).thenApply(v -> {
				isRegisterdForReads = true;
				return this;
			});
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}
	
	public CompletableFuture<Channel> unregisterForReads() {
		apiLog.trace(()->this+"Basic.unregisterForReads called");		
		try {
			isRegisterdForReads = false;
			return getSelectorManager().unregisterChannelForRead(this).thenApply(v -> this);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}	
           
	protected void setConnecting(boolean b) {
		isConnecting = b;
	}

	protected boolean isConnecting() {
		return isConnecting;
	}

	protected void setClosed(boolean b) {
		isClosed = b;
	}
    
    @Override
    public CompletableFuture<Channel> close() {
        //To prevent the following exception, in the readImpl method, we
        //check if the socket is already closed, and if it is we don't read
        //and just return -1 to indicate socket closed.
    	CompletableFuture<Channel> future = new CompletableFuture<>();
    	try {
    		apiLog.trace(()->this+"Basic.close called");
    		
	        if(!getRealChannel().isOpen()) {
	        	future.complete(this);
	        	return future;
	        }
	        
	        setClosed(true);
	        CloseRunnable runnable = new CloseRunnable(this, future);
	        unqueueAndFailWritesThenClose(runnable);
        } catch(Exception e) {
            log.error(this+"Exception closing channel", e);
            future.completeExceptionally(e);
        }
    	return future;
    }
    
    public void closeOnSelectorThread() throws IOException {
    	setClosed(true);
    	closeImpl();
    }
    protected abstract void closeImpl() throws IOException;

    public ChannelSession getSession() {
    	return session;
    }

	@Override
	public void setWriteTimeoutMs(int timeout) {
		this.writeTimeoutMs = timeout;
	}

	@Override
	public int getWriteTimeoutMs() {
		return writeTimeoutMs;
	}   
    
	@Override
	public void setMaxBytesWriteBackupSize(int maxQueueSize) {
		this.maxBytesWaitingSize = maxQueueSize;
	}
	
	@Override
	public int getMaxBytesBackupSize() {
		return maxBytesWaitingSize;
	}

	public boolean isRegisteredForReads() {
		return isRegisterdForReads;
	}
	
}
