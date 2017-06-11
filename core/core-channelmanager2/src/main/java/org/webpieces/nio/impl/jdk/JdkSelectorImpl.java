package org.webpieces.nio.impl.jdk;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

import org.webpieces.nio.api.exceptions.NioException;
import org.webpieces.nio.api.exceptions.RuntimeInterruptedException;
import org.webpieces.nio.api.jdk.JdkDatagramChannel;
import org.webpieces.nio.api.jdk.JdkSelect;
import org.webpieces.nio.api.jdk.JdkServerSocketChannel;
import org.webpieces.nio.api.jdk.JdkSocketChannel;
import org.webpieces.nio.api.jdk.Keys;
import org.webpieces.nio.api.jdk.SelectorListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;



/**
 */
public class JdkSelectorImpl implements JdkSelect
{
    private static final Logger log = LoggerFactory.getLogger(JdkSelectorImpl.class);
    private PollingThread thread;
    private AbstractSelector selector;
    private boolean running = false;
    private boolean wantToShutDown = false;
    private SelectorListener listener;
    private SelectorProvider provider;

    /**
     * Creates an instance of SelectorImpl.
     * @param provider
     */
    public JdkSelectorImpl(SelectorProvider provider) {
        this.provider = provider;
    }

    /**
     * @throws IOException 
     * @see org.webpieces.nio.api.jdk.ChannelsFactory#open()
     */
    public JdkSocketChannel open() throws IOException {
    	if(selector == null)
    		throw new IllegalArgumentException("start must be called first to start the thread up");
        java.nio.channels.SocketChannel channel = java.nio.channels.SocketChannel.open();
        return new JdkSocketChannelImpl(channel, selector);
    }

    /**
     * @see org.webpieces.nio.api.jdk.ChannelsFactory#open(java.nio.channels.SocketChannel)
     */
    public JdkSocketChannel open(java.nio.channels.SocketChannel newChan) {
    	if(selector == null)
    		throw new IllegalArgumentException("start must be called first to start the thread up");
    	
        return new JdkSocketChannelImpl(newChan, selector);
    }
    
	@Override
	public JdkServerSocketChannel openServerSocket() throws IOException {
		java.nio.channels.ServerSocketChannel channel = java.nio.channels.ServerSocketChannel.open();
		return new JdkServerSocketChannelImpl(channel, selector);
	}
	
	@Override
	public JdkDatagramChannel openDatagram() throws IOException {
		java.nio.channels.DatagramChannel channel = java.nio.channels.DatagramChannel.open();
		return new JdkDatagramChannelImpl(channel, selector);
	}
	
    /**
     * @see org.webpieces.nio.api.jdk.JdkSelect#wakeup()
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
    
    public void startPollingThread(SelectorListener l, String threadName) {
        if(running)
            throw new IllegalStateException("Already running, can't start again");        
        this.listener = l;
        try {
	        selector = provider.openSelector();
	        
	        thread = new PollingThread();
	        thread.setDaemon(true);
	        thread.setName(threadName);
	        thread.start();
        } catch(IOException e) {
        	throw new NioException(e);
        }
    }

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
                log.trace(()->"shutting down the PollingThread");
                selector.close();
                selector = null;
                thread = null;                

                synchronized(JdkSelectorImpl.this) {
                    running = false;
                    JdkSelectorImpl.this.notifyAll();
                }
            } catch (Exception e) {
                log.error("Exception on ConnectionManager thread", e);
            }
        }
    }
    
    protected void runLoop() {
        while (!wantToShutDown) {
            listener.selectorFired();
        }
    }

    /**
     * @see org.webpieces.nio.api.jdk.JdkSelect#getThread()
     */
    public Thread getThread() {
        return thread;
    }

    public Keys select() {
        try {
			int count = selector.select();
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			return new Keys(count, selectedKeys);
		} catch (IOException e) {
			throw new NioException(e);
		}
    }

    /**
     * @see org.webpieces.nio.api.jdk.JdkSelect#isRunning()
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @see org.webpieces.nio.api.jdk.JdkSelect#isWantShutdown()
     */
    public boolean isWantShutdown() {
        return wantToShutDown;
    }

    public void setRunning(boolean b) {
        running = b;
    }

	@Override
	public boolean isChannelOpen(SelectionKey key) {
		return key.channel().isOpen();
	}
    
}
