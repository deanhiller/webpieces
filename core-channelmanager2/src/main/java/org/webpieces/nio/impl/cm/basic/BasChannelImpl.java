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
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.exceptions.FailureInfo;
import org.webpieces.nio.api.exceptions.NioException;
import org.webpieces.nio.api.exceptions.RuntimeInterruptedException;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.impl.util.ChannelSessionImpl;
import org.webpieces.util.futures.Future;
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
	private LinkedBlockingQueue<DelayedWritesCloses> waitingWriters = new LinkedBlockingQueue<DelayedWritesCloses>(1000);
	private boolean isConnecting = false;
	private boolean isClosed = false;
    private boolean registered;
    
	public BasChannelImpl(IdObject id, SelectorManager2 selMgr) {
		super(id, selMgr);
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
   
    /**
     * This is the method where writes are added to the queue to be written later when the selector
     * fires and tells me we have room to write again.
     * 
     * @param id
     * @return true if the whole ByteBuffer was written, false if only part of it or none of it was written.
     * @throws IOException
     * @throws InterruptedException
     */
    private synchronized void tryWriteOrClose(DelayedWritesCloses action) {
    	try {
	        //TODO: make 30 seconds configurable in milliseoncds maybe
	        boolean accepted = waitingWriters.offer(action, 30, TimeUnit.SECONDS);
	        if(!accepted) {
	        	throw new RuntimeException(this+"registered="+registered+" Dropping data, the upstream must be full as our queue is full of writes" +
	        			" that are stuck and can't go out(you should NOT call dataChunk.setProcessed in this case so the" +
	        			" downstream will slowdown and will not flood you as tcp flow control automatically kicks" +
	        			" in which means you will not flood the upstream like you are doing!!!!");
	        }
	        
	        //if not already registered, then register for writes.....
	        //NOTE: we must do this after waitingWriters.offer so there is something on the queue to read
	        //otherwise, that could be bad.
	        if(!registered) {
	            registered = true;
	            if(log.isTraceEnabled())
	                log.trace(this+"registering channel for write msg cb="+action+" size="+waitingWriters.size());
	            getSelectorManager().registerSelectableChannel(this, SelectionKey.OP_WRITE, null, false);
	        }
    	} catch(InterruptedException e) {
    		throw new RuntimeInterruptedException(e);
    	}
    }
       
    /**
     * This method is reading from the queue and writing out to the socket buffers that
     * did not get written out when client called write.
     *
     */
    synchronized void writeAll() {
        Queue<DelayedWritesCloses> writers = waitingWriters;

        if(writers.isEmpty())
            return;

        while(!writers.isEmpty()) {
            DelayedWritesCloses writer = writers.peek();
            boolean finished = writer.runDelayedAction(true);
            if(!finished) {
                if(log.isTraceEnabled())
                    log.trace(this+"Did not write all of id="+writer);
                break;
            }
            //if it finished, remove the item from the queue.  It
            //does not need to be run again.
            writers.remove();
        }
        
        if(writers.isEmpty()) {
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
		return writeNewImpl(b);
	}

	private Future<Channel, FailureInfo> writeNewImpl(ByteBuffer b) {
		if(!getSelectorManager().isRunning())
			throw new IllegalStateException(this+"ChannelManager must be running and is stopped");		
		else if(isClosed) {
			AsynchronousCloseException exc = new AsynchronousCloseException();
			throw new NioException(this+"Client cannot write after the client closed the socket", exc);
		}
		PromiseImpl<Channel, FailureInfo> impl = new PromiseImpl<>();
		
		if(apiLog.isTraceEnabled())
			apiLog.trace(this+"Basic.write");
		
		int totalToWriteOut = b.remaining();
		int written = writeImpl(b);
		if(written != totalToWriteOut) {
			if(b.remaining() + written != totalToWriteOut) {
				throw new IllegalStateException("Something went wrong.  b.remaining()="+b.remaining()+" written="+written+" total="+totalToWriteOut);
			}
			WriteRunnable holder = new WriteRunnable(this, b, impl);
			tryWriteOrClose(holder);
	        if(log.isTraceEnabled()) {
	        	log.trace(this+"sent write to queue");
	       	}
		} else if(log.isTraceEnabled()) {
			impl.setResult(this);
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
        //
        //This is very complicated.  It must be done after all the writes that have already
        //been called before the close was called.  Basically, the close may need be 
        //queued if there are writes on the queue.
        //unless you like to see the following
        //exception..........
        //Feb 19, 2006 6:06:03 AM biz.xsoftware.test.nio.tcp.ZNioSuperclassTest verifyTearDown
        //INFO: CLIENT1 CLOSE
        //Feb 19, 2006 6:06:03 AM biz.xsoftware.impl.nio.cm.basic.Helper read
        //INFO: [[client]] Exception
        //java.nio.channels.ClosedChannelException
        //  at sun.nio.ch.SocketChannelImpl.ensureReadOpen(SocketChannelImpl.java:112)
        //  at sun.nio.ch.SocketChannelImpl.read(SocketChannelImpl.java:139)
        //  at biz.xsoftware.impl.nio.cm.basic.TCPChannelImpl.readImpl(TCPChannelImpl.java:162)
        //  at biz.xsoftware.impl.nio.cm.basic.Helper.read(Helper.java:143)
        //  at biz.xsoftware.impl.nio.cm.basic.Helper.processKey(Helper.java:92)
        //  at biz.xsoftware.impl.nio.cm.basic.Helper.processKeys(Helper.java:47)
        //  at biz.xsoftware.impl.nio.cm.basic.SelectorManager2.runLoop(SelectorManager2.java:305)
        //  at biz.xsoftware.impl.nio.cm.basic.SelectorManager2$PollingThread.run(SelectorManager2.java:267)
    	PromiseImpl<Channel, FailureInfo> future = new PromiseImpl<>();
    	try {
    		if(apiLog.isTraceEnabled())
    			apiLog.trace(this+"Basic.close called");
    		
	        if(!getRealChannel().isOpen())
	        	future.setResult(this);
	        
	        setClosed(true);
	        CloseRunnable runnable = new CloseRunnable(this, future);
	        tryWriteOrClose(runnable);
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
}
