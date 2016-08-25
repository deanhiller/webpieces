package org.webpieces.nio.api.testutil.nioapi;

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

    void startPollingThread(SelectorListener manager2, String threadName);

    /**
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

    void setRunning(boolean b);

    SelectionKey getKeyFromChannel(SelectableChannel realChannel);

    SelectionKey register(SelectableChannel s, int allOps, Object struct);

}
