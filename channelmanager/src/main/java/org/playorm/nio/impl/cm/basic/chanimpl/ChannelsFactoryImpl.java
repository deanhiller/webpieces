package org.playorm.nio.impl.cm.basic.chanimpl;

import java.io.IOException;

import org.playorm.nio.api.testutil.chanapi.ChannelsFactory;
import org.playorm.nio.api.testutil.chanapi.SocketChannel;


/**
 */
public class ChannelsFactoryImpl implements ChannelsFactory
{

    /**
     * @throws IOException 
     * @see org.playorm.nio.api.testutil.chanapi.ChannelsFactory#open()
     */
    public SocketChannel open() throws IOException {
        java.nio.channels.SocketChannel channel = java.nio.channels.SocketChannel.open();
        return new SocketChannelImpl(channel);
    }

    /**
     * @see org.playorm.nio.api.testutil.chanapi.ChannelsFactory#open(java.nio.channels.SocketChannel)
     */
    public SocketChannel open(java.nio.channels.SocketChannel newChan) {
        return new SocketChannelImpl(newChan);
    }

}
