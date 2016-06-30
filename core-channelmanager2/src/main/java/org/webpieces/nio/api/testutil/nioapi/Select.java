package org.webpieces.nio.api.testutil.nioapi;

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
     */
    void startPollingThread(SelectorListener manager2, String threadName);

    /**
     * @throws InterruptedException 
     * 
     */
    void stopPollingThread();

    /**
     */
    Object getThread();

    /**
     */
    Set<SelectionKey> selectedKeys();

    /**
     */
    int select();

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
     * @param realChannel
     */
    SelectionKey getKeyFromChannel(SelectableChannel realChannel);

    /**
     * @param s
     * @param allOps
     * @param struct
     * @throws ClosedChannelException 
     */
    SelectionKey register(SelectableChannel s, int allOps, Object struct);

}
