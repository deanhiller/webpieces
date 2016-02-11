package org.playorm.nio.api.testutil.chanapi;

import java.io.IOException;

/**
 */
public interface ChannelsFactory
{

    /**
     * @return the newly opened SocketChannel
     * @throws IOException 
     */
    SocketChannel open() throws IOException;

    /**
     * @param newChan
     * @return the newly opened SocketChannel
     */
    SocketChannel open(java.nio.channels.SocketChannel newChan);

}
