package org.playorm.nio.impl.cm.basic;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.NioException;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.handlers.FutureOperation;
import org.playorm.nio.api.handlers.OperationCallback;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.ChannelSession;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.impl.util.FutureOperationImpl;
import org.playorm.nio.impl.util.UtilWaitForCompletion;



/**
 * @author Dean Hiller
 */
public abstract class BasChannelImpl
	extends RegisterableChannelImpl
	implements Channel {

	private static final Logger apiLog = Logger.getLogger(Channel.class.getName());
    private static final Logger log = Logger.getLogger(BasChannelImpl.class.getName());
    private static final FactoryCreator CREATOR = FactoryCreator.createFactory(null);
    
    private ChannelSession session;
	private LinkedBlockingQueue<DelayedWritesCloses> waitingWriters = new LinkedBlockingQueue<DelayedWritesCloses>(1000);
	private boolean isConnecting = false;
	private boolean isClosed = false;
    private boolean registered;
    
	public BasChannelImpl(IdObject id, BufferFactory factory, SelectorManager2 selMgr) {
		super(id, selMgr);
		session = CREATOR.createSession(this);  
	}
	
	/* (non-Javadoc)
	 * @see biz.xsoftware.nio.RegisterableChannelImpl#getRealChannel()
	 */
	public abstract SelectableChannel getRealChannel();

	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.RegisterableChannel#isBlocking()
	 */
	public abstract boolean isBlocking();

	public abstract int readImpl(ByteBuffer b) throws IOException;
	protected abstract int writeImpl(ByteBuffer b) throws IOException;
   
    /**
     * This is the method where writes are added to the queue to be written later when the selector
     * fires and tells me we have room to write again.
     * 
     * @param id
     * @return true if the whole ByteBuffer was written, false if only part of it or none of it was written.
     * @throws IOException
     * @throws InterruptedException
     */
    private synchronized void tryWriteOrClose(DelayedWritesCloses action) throws IOException, InterruptedException {       
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
            if(log.isLoggable(Level.FINER))
                log.finer(this+"registering channel for write msg cb="+action+" size="+waitingWriters.size());
            getSelectorManager().registerSelectableChannel(this, SelectionKey.OP_WRITE, null, false);
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
                if(log.isLoggable(Level.FINER))
                    log.finer(this+"Did not write all of id="+writer);
                break;
            }
            //if it finished, remove the item from the queue.  It
            //does not need to be run again.
            writers.remove();
        }
        
        if(writers.isEmpty()) {
            if(log.isLoggable(Level.FINER))
                log.fine(this+"unregister writes");
            registered = false;
            Helper.unregisterSelectableChannel(this, SelectionKey.OP_WRITE); 
        }
    }

    public void bind(SocketAddress addr) {
        if(!(addr instanceof InetSocketAddress))
            throw new IllegalArgumentException(this+"Can only bind to InetSocketAddress addressses");
        if(apiLog.isLoggable(Level.FINE))
        	apiLog.fine(this+"Basic.bind called addr="+addr);
        
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

		if(apiLog.isLoggable(Level.FINE))
			apiLog.fine(this+"Basic.registerForReads called");
		
        try {
			getSelectorManager().registerChannelForRead(this, listener);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}
	
	public void unregisterForReads() {
		if(apiLog.isLoggable(Level.FINE))
			apiLog.fine(this+"Basic.unregisterForReads called");		
		try {
			getSelectorManager().unregisterChannelForRead(this);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}	

	public int oldWrite(ByteBuffer b) {
		try {
			return oldWriteImpl(b);
		} catch (IOException e) {
			throw new NioException(e);
		}
	}
	private int oldWriteImpl(ByteBuffer b) throws IOException {
		if(!getSelectorManager().isRunning())
			throw new IllegalStateException(this+"ChannelManager must be running and is stopped");
		else if(isClosed) {
			AsynchronousCloseException exc = new AsynchronousCloseException();
			IOException ioe = new IOException(this+"Client cannot write after the client closed the socket");
			exc.initCause(ioe);
			throw exc;
		}
		Object t = getSelectorManager().getThread();
		if(Thread.currentThread().equals(t)) {
			//leave this in, users should not do this....
			throw new RuntimeException(this+"You should not perform a " +
					"blocking write on the channelmanager thread unless you like deadlock.  " +
					"Use the cm threading layer, or put the code calling this write on another thread");
		}

		try {
            int remain = b.remaining();

			UtilWaitForCompletion waitWrite = new UtilWaitForCompletion(this, t);
			oldWrite(b, waitWrite);
            //otherwise if not all was written, wait for completion as it was added to queue
            //which writes on selector thread....
			waitWrite.waitForComplete();
            
			if(b.hasRemaining())
				throw new RuntimeException(this+"Did not write all of the ByteBuffer out");
			return remain;
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public FutureOperation write(ByteBuffer b) {
		try {
			return writeNewImpl(b);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}

	private FutureOperation writeNewImpl(ByteBuffer b) throws IOException, InterruptedException {
		if(!getSelectorManager().isRunning())
			throw new IllegalStateException(this+"ChannelManager must be running and is stopped");		
		else if(isClosed) {
			AsynchronousCloseException exc = new AsynchronousCloseException();
			IOException ioe = new IOException(this+"Client cannot write after the client closed the socket");
			exc.initCause(ioe);
			throw exc;
		}
		FutureOperationImpl impl = new FutureOperationImpl();
		
		if(apiLog.isLoggable(Level.FINER))
			apiLog.finer(this+"Basic.write");
		
        //copy the buffer here
        ByteBuffer newOne = ByteBuffer.allocate(b.remaining());
        newOne.put(b);
        newOne.flip();
        WriteRunnable holder = new WriteRunnable(this, newOne, impl);
        
		tryWriteOrClose(holder);
        
        if(log.isLoggable(Level.FINER)) {
        	log.finest(this+"sent write to queue");
       	}
        return impl;
	}

	public void oldWrite(ByteBuffer b, OperationCallback h) {
		try {
			oldWriteImpl(b, h);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}
	public void oldWriteImpl(ByteBuffer b, OperationCallback h) throws IOException, InterruptedException {
		if(!getSelectorManager().isRunning())
			throw new IllegalStateException(this+"ChannelManager must be running and is stopped");		
		else if(isClosed) {
			AsynchronousCloseException exc = new AsynchronousCloseException();
			IOException ioe = new IOException(this+"Client cannot write after the client closed the socket");
			exc.initCause(ioe);
			throw exc;
		}
		if(apiLog.isLoggable(Level.FINER))
			apiLog.finer(this+"Basic.write callback="+h);
		
        //copy the buffer here
        ByteBuffer newOne = ByteBuffer.allocate(b.remaining());
        newOne.put(b);
        newOne.flip();
        WriteRunnable holder = new WriteRunnable(this, newOne, h);
        
		tryWriteOrClose(holder);
        
        if(log.isLoggable(Level.FINER)) {
        	log.finest(this+"sent write to queue");
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

    /* (non-Javadoc)
     * @see api.biz.xsoftware.nio.SocketChannel#close()
     */
    public void oldClose() {                
        
        Object t = getSelectorManager().getThread();
        if(t != null && Thread.currentThread().equals(t)) {
            //leave this in, users should not do this....
            throw new RuntimeException(this+"You should not perform a blocking close "+
        "on the channelmanager thread for performance reasons.  Use the cm threading layer, "+
        "or put the code calling this write on another thread");
        }
        try {
            UtilWaitForCompletion waitWrite = new UtilWaitForCompletion(this, null);
            oldClose(waitWrite);
            waitWrite.waitForComplete();
        } catch(Exception e) {
            log.log(Level.WARNING, this+"Exception closing channel", e);
        }
    }
    
    public void oldClose(OperationCallback h) {
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
    	try {
    		if(apiLog.isLoggable(Level.FINE))
    			apiLog.fine(this+"Basic.close called");
    		
	        if(!getRealChannel().isOpen())
	            h.finished(this);
	        
	        setClosed(true);
	        CloseRunnable runnable = new CloseRunnable(this, h);
	        tryWriteOrClose(runnable);
        } catch(Exception e) {
            log.log(Level.WARNING, this+"Exception closing channel", e);
        }
    }
    
    @Override
    public FutureOperation close() {
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
    	FutureOperationImpl future = new FutureOperationImpl();
    	try {
    		if(apiLog.isLoggable(Level.FINE))
    			apiLog.fine(this+"Basic.close called");
    		
	        if(!getRealChannel().isOpen())
	            future.finished(this);
	        
	        setClosed(true);
	        CloseRunnable runnable = new CloseRunnable(this, future);
	        tryWriteOrClose(runnable);
        } catch(Exception e) {
            log.log(Level.WARNING, this+"Exception closing channel", e);
            future.failed(this, e);
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
