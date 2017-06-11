package org.webpieces.nio.api.jdk;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 */
public interface JdkSelect
{
    /**
     * @return the newly opened SocketChannel
     * @throws IOException 
     */
    JdkSocketChannel open() throws IOException;

    /**
     * @param newChan
     * @return the newly opened SocketChannel
     */
    JdkSocketChannel open(java.nio.channels.SocketChannel newChan);
    
	JdkServerSocketChannel openServerSocket() throws IOException;

	JdkDatagramChannel openDatagram() throws IOException;

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
    Keys select();

    /**
     */
    boolean isRunning();

    /**
     */
    boolean isWantShutdown();

	boolean isChannelOpen(SelectionKey key);

}
