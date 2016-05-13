package org.webpieces.nio.impl.cm.basic.nioimpl;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;
import org.webpieces.nio.api.exceptions.NioException;
import org.webpieces.nio.api.exceptions.RuntimeInterruptedException;
import org.webpieces.nio.api.testutil.nioapi.Select;
import org.webpieces.nio.api.testutil.nioapi.SelectorListener;



/**
 */
public class SelectorImpl implements Select
{
    private static final Logger log = LoggerFactory.getLogger(SelectorImpl.class);
    private PollingThread thread;
    private AbstractSelector selector;
    private boolean running = false;
    private boolean wantToShutDown = false;
    private SelectorListener listener;
    private SelectorProvider provider;
    
    /**
     * Creates an instance of SelectorImpl.
     * @param selector
     */
    public SelectorImpl(AbstractSelector selector) {
    }

    /**
     * Creates an instance of SelectorImpl.
     * @param provider
     */
    public SelectorImpl(SelectorProvider provider) {
        this.provider = provider;
    }

    /**
     * @see org.webpieces.nio.api.testutil.nioapi.Select#wakeup()
     */
    public void wakeup() {
        if(selector != null)
            selector.wakeup();
    }
    
    /**
     */
    public Selector getSelector() {
        return selector;
    }
    /**
     * @throws IOException 
     * @see org.webpieces.nio.api.testutil.nioapi.Select#startPollingThread(org.webpieces.nio.impl.cm.basic.SelectorManager2)
     */
    public void startPollingThread(SelectorListener l, String threadName) {
        if(running)
            throw new IllegalStateException("Already running, can't start again");        
        this.listener = l;
        try {
	        selector = provider.openSelector();
	        
	        thread = new PollingThread();
	        thread.setName(threadName);
	        thread.start();
        } catch(IOException e) {
        	throw new NioException(e);
        }
    }

    /**
     * @throws InterruptedException 
     * @see org.webpieces.nio.api.testutil.nioapi.Select#stopPollingThread()
     */
    public void stopPollingThread() {
        if(!running)
            return;
        
        wantToShutDown = true;
        selector.wakeup();
        
        synchronized(this) {
            if(running) {
            	try {
            		this.wait(20000);
            	} catch(InterruptedException e) {
            		throw new RuntimeInterruptedException(e);
            	}
            }
            if(running)
                log.error("Tried to shutdown channelmanager, but it took longer " +
                        "than 20 seconds.  It may be hung now");
        }        
    }

    //protect the Thread from being started or controlled by putting
    //the run in a private class.  The rest of the methods are protected
    //so they are ok.
    private class PollingThread extends Thread {
        @Override
        public void run() {
            try {           
                running = true;
                runLoop();
                if(log.isTraceEnabled())
                    log.trace("shutting down the PollingThread");
                selector.close();
                selector = null;
                thread = null;                

                synchronized(SelectorImpl.this) {
                    running = false;
                    SelectorImpl.this.notifyAll();
                }
            } catch (Exception e) {
                log.warn("Exception on ConnectionManager thread", e);
            }
        }
    }
    
    protected void runLoop() {
        while (!wantToShutDown) {
            listener.selectorFired();
        }
    }

    /**
     * @see org.webpieces.nio.api.testutil.nioapi.Select#getThread()
     */
    public Object getThread() {
        return thread;
    }

    /**
     * @see org.webpieces.nio.api.testutil.nioapi.Select#selectedKeys()
     */
    public Set<SelectionKey> selectedKeys() {
        return selector.selectedKeys();
    }

    /**
     * @throws IOException 
     * @see org.webpieces.nio.api.testutil.nioapi.Select#select()
     */
    public int select() {
        try {
			return selector.select();
		} catch (IOException e) {
			throw new NioException(e);
		}
    }

    /**
     * @see org.webpieces.nio.api.testutil.nioapi.Select#isRunning()
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @see org.webpieces.nio.api.testutil.nioapi.Select#isWantShutdown()
     */
    public boolean isWantShutdown() {
        return wantToShutDown;
    }

    /**
     * @see org.webpieces.nio.api.testutil.nioapi.Select#setRunning(boolean)
     */
    public void setRunning(boolean b) {
        running = b;
    }

    /**
     * @see org.webpieces.nio.api.testutil.nioapi.Select#getKeyFromChannel(java.nio.channels.SelectableChannel)
     */
    public SelectionKey getKeyFromChannel(SelectableChannel realChannel) {
        return realChannel.keyFor(selector);
    }

    /**
     * @throws ClosedChannelException 
     * @see org.webpieces.nio.api.testutil.nioapi.Select#register(java.nio.channels.SelectableChannel,
     *                       int, org.webpieces.nio.impl.cm.basic.WrapperAndListener)
     */
    public SelectionKey register(SelectableChannel s, int allOps, Object struct) {
    	if(struct == null)
    		throw new IllegalArgumentException("struct cannot be null");
    	
        try {
			return s.register(selector, allOps, struct);
		} catch (ClosedChannelException e) {
			throw new NioClosedChannelException(e);
		}
    }
    
}
