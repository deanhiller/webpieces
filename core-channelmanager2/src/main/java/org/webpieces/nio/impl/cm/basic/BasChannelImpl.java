package org.webpieces.nio.impl.cm.basic;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.exceptions.FailureInfo;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;
import org.webpieces.nio.api.exceptions.NioException;
import org.webpieces.nio.api.exceptions.NioTimeoutException;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.impl.util.ChannelSessionImpl;
import org.webpieces.util.futures.Future;
import org.webpieces.util.futures.Promise;
import org.webpieces.util.futures.PromiseImpl;

/**
 * @author Dean Hiller
 */
public abstract class BasChannelImpl
	extends RegisterableChannelImpl
	implements Channel {

	private static final Logger apiLog = LoggerFactory.getLogger(Channel.class);
	private static final Logger log = LoggerFactory.getLogger(BasChannelImpl.class);
	
    private ChannelSession session = new ChannelSessionImpl();
	private ConcurrentLinkedQueue<WriteRunnable> waitingWriters = new ConcurrentLinkedQueue<WriteRunnable>();
	private boolean isConnecting = false;
	private boolean isClosed = false;
    private boolean registered;
	private boolean doNotAllowWrites;
	private int writeTimeoutMs = 3_000;
	private int count;
	
	public BasChannelImpl(IdObject id, SelectorManager2 selMgr, Executor executor) {
		super(id, selMgr, executor);
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
   
    private void unqueueAndFailWritesThenClose(CloseRunnable action) {
    	List<Promise<Channel, FailureInfo>> promises;
    	synchronized(this) { //put here for emphasis that we are synchronizing here but not below
			promises = failAllWritesInQueue();
    	}
    	
		//TODO: This should really be inlined now.  It's a remnant of an old design since close didn't
		//work well outside the selector thread previously
		action.runDelayedAction();
		
		//we used to do this to put the close on the selector thread but if writes are held up it won't work
    	//registerForWritesOrClose();
    	
    	//notify clients outside the synchronization block!!!
		for(Promise<Channel, FailureInfo> promise : promises) {
    		log.info("WRITES outstanding while close was called, notifying client through his failure method of the exception");
    		//we only incur the cost of Throwable.fillInStackTrace() if we will use this exception
    		//(it's called in the Throwable constructor) so we don't do this on every close channel
        	NioClosedChannelException closeExc = new NioClosedChannelException("There are "+promises.size()
        			+" writes that are not complete yet(you called write but "
        			+ "they did not call success back to the client).");
        	promise.setFailure(new FailureInfo(this, closeExc));
		}
    }
    
    /**
     * This is the method where writes are added to the queue to be written later when the selector
     * fires and tells me we have room to write again.
     * 
     * @param id
     * @return true if the whole ByteBuffer was written, false if only part of it or none of it was written.
     * @throws IOException
     * @throws InterruptedException
     */
	private void queueTheWrite(WriteRunnable action) {
		List<Promise<Channel, FailureInfo>> promisesToFail = null;
		String msg = null;
		synchronized (this) {
			count++;
			WriteRunnable lastVal = waitingWriters.peek();
			if(lastVal != null) {
				long currentTime = System.currentTimeMillis();
				long delay = currentTime - lastVal.getCreationTime();
				if(delay > writeTimeoutMs) {
					msg = "delay="+delay+" timeout="+writeTimeoutMs;
					log.info("FAILING all writes in the queue due to timeout. "+msg);
					promisesToFail = failAllWritesInQueue();
				}
			}			
		}

		//MUST be outside the synchronization block to notify clients so we don't deadlock
		if(promisesToFail != null) {
			log.info("FAILING this write in the queue due to timeout. sending client the exception to his failure function");
			for(Promise<Channel, FailureInfo> promise : promisesToFail) {
	    		//we only incur the cost of Throwable.fillInStackTrace() if we will use this exception
	    		//(it's called in the Throwable constructor) so we don't do this on every close channel
				NioTimeoutException exc = new NioTimeoutException("The write at the beginning of the\n"
				+ "queue has timed out (which fails all writes behind it).  You probably need\n"
				+ " to throttle your app from writing downstream too\n fast. write queue size="
				+ promisesToFail.size()+" "+msg+"\nThe reason it is probably you need throttling is\n"
				+ "that writing to your computers nic outgoing buffer should be in <1ms so generally 3 secs is\n"
				+ "a fine timeout.  The timeout is not remote but local writing to outoing nic buffer which\n"
				+ "must be full at this point if you are seeing a timeout");
	        	promise.setFailure(new FailureInfo(this, exc));
			}
			return;
		}
		
		boolean accepted = waitingWriters.add(action);
		if(!accepted) {
			throw new RuntimeException(this+"registered="+registered+" This should never occur");
		}
		
		registerForWritesOrClose();
	}
	
	//synchronized with writeAll as both try to go through every element in the queue
	//while most of the time there will be no contention(only on the close do we hit this)
	private synchronized List<Promise<Channel, FailureInfo>> failAllWritesInQueue() {
		doNotAllowWrites = true;
		List<Promise<Channel, FailureInfo>> copy = new ArrayList<>();
		while(!waitingWriters.isEmpty()) {
			WriteRunnable runnable = waitingWriters.remove();
			runnable.markBufferRead(); //in case clients releaseBuffer
			copy.add(runnable.getPromise());
		}
		return copy;
	}

	private void registerForWritesOrClose() {
		//if not already registered, then register for writes.....
        //NOTE: we must do this after waitingWriters.offer so there is something on the queue to read
        //otherwise, that could be bad.
        if(!registered) {
            registered = true;
            if(log.isTraceEnabled())
                log.trace(this+"registering channel for write msg. size="+waitingWriters.size());
            getSelectorManager().registerSelectableChannel(this, SelectionKey.OP_WRITE, null, false);
        }
	}
       
    /**
     * This method is reading from the queue and writing out to the socket buffers that
     * did not get written out when client called write.
     *
     */
	 void writeAll() {
		List<Promise<Channel, FailureInfo>> finishedPromises = new ArrayList<>();
		synchronized(this) {
	        if(waitingWriters.isEmpty())
	            return;
	
	        while(!waitingWriters.isEmpty()) {
	            WriteRunnable writer = waitingWriters.peek();
	            boolean finished = writer.runDelayedAction();
	            if(!finished) {
	                if(log.isTraceEnabled())
	                    log.trace(this+"Did not write all of id="+writer);
	                break;
	            }
	            //if it finished, remove the item from the queue.  It
	            //does not need to be run again.
	            waitingWriters.poll();
	            finishedPromises.add(writer.getPromise());
	        }
		}
		
        //MAKE SURE to notify clients outside of synchronization block so no deadlocks with their locks
        for(Promise<Channel, FailureInfo> promise : finishedPromises) {
        	promise.setResult(this);
        }
        
        if(waitingWriters.isEmpty()) {
        	if(log.isTraceEnabled())
        		log.trace(this+"unregister writes");
            registered = false;
            Helper.unregisterSelectableChannel(this, SelectionKey.OP_WRITE); 
        }
    }

    public void bind(SocketAddress addr) {
        if(!(addr instanceof InetSocketAddress))
            throw new IllegalArgumentException(this+"Can only bind to InetSocketAddress addressses");
        if(apiLog.isTraceEnabled())
        	apiLog.trace(this+"Basic.bind called addr="+addr);
        
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
    
	public void registerForReads(DataListener listener) {
		if(listener == null)
			throw new IllegalArgumentException(this+"listener cannot be null");
		else if(!isConnecting && !isConnected()) {
			throw new IllegalStateException(this+"Must call one of the connect methods first(ie. connect THEN register for reads)");
		}

		if(apiLog.isTraceEnabled())
			apiLog.trace(this+"Basic.registerForReads called");
		
        try {
			getSelectorManager().registerChannelForRead(this, listener);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}
	
	public void unregisterForReads() {
		if(apiLog.isTraceEnabled())
			apiLog.trace(this+"Basic.unregisterForReads called");		
		try {
			getSelectorManager().unregisterChannelForRead(this);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}	

	@Override
	public Future<Channel, FailureInfo> write(ByteBuffer b) {
		if(!getSelectorManager().isRunning())
			throw new IllegalStateException(this+"ChannelManager must be running and is stopped");		
		else if(isClosed) {
			AsynchronousCloseException exc = new AsynchronousCloseException();
			throw new NioException(this+"Client cannot write after the client closed the socket", exc);
		}
		PromiseImpl<Channel, FailureInfo> impl = new PromiseImpl<>(executor);
		
		if(apiLog.isTraceEnabled())
			apiLog.trace(this+"Basic.write");
		
		if(doNotAllowWrites) {
			throw new IllegalStateException("This channel is in a failed state.  "
					+ "failure functions were called so look for exceptions from them");
		}
		
		int totalToWriteOut = b.remaining();
		int written = writeImpl(b);
		if(written != totalToWriteOut) {
			if(b.remaining() + written != totalToWriteOut) {
				throw new IllegalStateException("Something went wrong.  b.remaining()="+b.remaining()+" written="+written+" total="+totalToWriteOut);
			}
			WriteRunnable holder = new WriteRunnable(this, b, impl, System.currentTimeMillis());
			queueTheWrite(holder);
	        if(log.isTraceEnabled()) {
	        	log.trace(this+"sent write to queue");
	       	}
		} else {
			impl.setResult(this);
			if(log.isTraceEnabled())
				log.trace(this+" wrote bytes on client thread");
		}

        return impl;
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
    public Future<Channel, FailureInfo> close() {
        //To prevent the following exception, in the readImpl method, we
        //check if the socket is already closed, and if it is we don't read
        //and just return -1 to indicate socket closed.
    	PromiseImpl<Channel, FailureInfo> future = new PromiseImpl<>(executor);
    	try {
    		if(apiLog.isTraceEnabled())
    			apiLog.trace(this+"Basic.close called");
    		
	        if(!getRealChannel().isOpen())
	        	future.setResult(this);
	        
	        setClosed(true);
	        CloseRunnable runnable = new CloseRunnable(this, future);
	        unqueueAndFailWritesThenClose(runnable);
        } catch(Exception e) {
            log.warn(this+"Exception closing channel", e);
            future.setFailure(new FailureInfo(this, e));
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
    
    
}
