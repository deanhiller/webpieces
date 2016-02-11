package org.playorm.nio.api.testutil.nioapi;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.Set;


/**
 */
public interface Select
{

    /**
     * 
     */
    void wakeup();

    /**
     * @param manager2
     * @throws IOException 
     */
    void startPollingThread(SelectorListener manager2) throws IOException;

    /**
     * @throws InterruptedException 
     * 
     */
    void stopPollingThread() throws InterruptedException;

    /**
     */
    Object getThread();

    /**
     */
    Set<SelectionKey> selectedKeys();

    /**
     * @throws IOException 
     */
    int select() throws IOException;

    /**
     */
    boolean isRunning();

    /**
     */
    boolean isWantShutdown();

    /**
     * @param b
     */
    void setRunning(boolean b);

    /**
     * @param id
     * @param r
     * @param selector
     */
    ChannelRegistrationListener createRegistrationListener(Object id, SelectorRunnable r, Object selector);

    /**
     * @param realChannel
     */
    SelectionKey getKeyFromChannel(SelectableChannel realChannel);

    /**
     * @param s
     * @param allOps
     * @param struct
     * @throws ClosedChannelException 
     */
    SelectionKey register(SelectableChannel s, int allOps, Object struct) throws ClosedChannelException;

}
