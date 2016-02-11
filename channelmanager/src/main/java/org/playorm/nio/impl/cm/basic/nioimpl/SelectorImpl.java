package org.playorm.nio.impl.cm.basic.nioimpl;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.testutil.nioapi.ChannelRegistrationListener;
import org.playorm.nio.api.testutil.nioapi.Select;
import org.playorm.nio.api.testutil.nioapi.SelectorListener;
import org.playorm.nio.api.testutil.nioapi.SelectorRunnable;
import org.playorm.nio.impl.cm.basic.SelectorManager2;



/**
 */
public class SelectorImpl implements Select
{
    private static final Logger log = Logger.getLogger(SelectorImpl.class.getName());
    private PollingThread thread;
    private AbstractSelector selector;
    private String id;
    private boolean running = false;
    private boolean wantToShutDown = false;
    private SelectorListener listener;
    private SelectorProvider provider;
    
    /**
     * Creates an instance of SelectorImpl.
     * @param selector
     */
    public SelectorImpl(String id, AbstractSelector selector) {

    }

    /**
     * Creates an instance of SelectorImpl.
     * @param provider
     */
    public SelectorImpl(String id, SelectorProvider provider) {
        this.id = id;
        this.provider = provider;
    }

    /**
     * @see org.playorm.nio.api.testutil.nioapi.Select#wakeup()
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
     * @see org.playorm.nio.api.testutil.nioapi.Select#startPollingThread(org.playorm.nio.impl.cm.basic.SelectorManager2)
     */
    public void startPollingThread(SelectorListener l) throws IOException {
        if(running)
            throw new IllegalStateException("Already running, can't start again");        
        this.listener = l;
        selector = provider.openSelector();
        
        thread = new PollingThread();
        thread.setName("SelMgr-"+id);
        thread.start();
    }

    /**
     * @throws InterruptedException 
     * @see org.playorm.nio.api.testutil.nioapi.Select#stopPollingThread()
     */
    public void stopPollingThread() throws InterruptedException {
        if(!running)
            return;
        
        wantToShutDown = true;
        selector.wakeup();
        
        synchronized(this) {
            if(running)
                this.wait(20000);
            if(running)
                log.severe(id+"Tried to shutdown channelmanager, but it took longer " +
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
                if(log.isLoggable(Level.FINE))
                    log.fine("shutting down the PollingThread");
                selector.close();
                selector = null;
                thread = null;                

                synchronized(SelectorImpl.this) {
                    running = false;
                    SelectorImpl.this.notifyAll();
                }
            } catch (Exception e) {
                log.log(Level.WARNING, id+"Exception on ConnectionManager thread", e);
            }
        }
    }
    
    protected void runLoop() {
        while (!wantToShutDown) {
            listener.selectorFired();
        }
    }

    /**
     * @see org.playorm.nio.api.testutil.nioapi.Select#getThread()
     */
    public Object getThread() {
        return thread;
    }

    /**
     * @see org.playorm.nio.api.testutil.nioapi.Select#selectedKeys()
     */
    public Set<SelectionKey> selectedKeys() {
        return selector.selectedKeys();
    }

    /**
     * @throws IOException 
     * @see org.playorm.nio.api.testutil.nioapi.Select#select()
     */
    public int select() throws IOException {
        return selector.select();
    }

    /**
     * @see org.playorm.nio.api.testutil.nioapi.Select#isRunning()
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @see org.playorm.nio.api.testutil.nioapi.Select#isWantShutdown()
     */
    public boolean isWantShutdown() {
        return wantToShutDown;
    }

    /**
     * @see org.playorm.nio.api.testutil.nioapi.Select#setRunning(boolean)
     */
    public void setRunning(boolean b) {
        running = b;
    }

    /**
     * @see org.playorm.nio.api.testutil.nioapi.Select#createRegistrationListener
     * (java.lang.Object, org.playorm.nio.api.testutil.nioapi.SelectorRunnable, org.playorm.nio.impl.cm.basic.SelectorManager2)
     */
    public ChannelRegistrationListener createRegistrationListener(Object id, SelectorRunnable r, Object s) {
        return new RegistrationListenerImpl(id, r, (SelectorManager2)s);
    }

    /**
     * @see org.playorm.nio.api.testutil.nioapi.Select#getKeyFromChannel(java.nio.channels.SelectableChannel)
     */
    public SelectionKey getKeyFromChannel(SelectableChannel realChannel) {
        return realChannel.keyFor(selector);
    }

    /**
     * @throws ClosedChannelException 
     * @see org.playorm.nio.api.testutil.nioapi.Select#register(java.nio.channels.SelectableChannel,
     *                       int, org.playorm.nio.impl.cm.basic.WrapperAndListener)
     */
    public SelectionKey register(SelectableChannel s, int allOps, Object struct) throws ClosedChannelException {
    	if(struct == null)
    		throw new IllegalArgumentException("struct cannot be null");
    	
        return s.register(selector, allOps, struct);
    }
    
}
