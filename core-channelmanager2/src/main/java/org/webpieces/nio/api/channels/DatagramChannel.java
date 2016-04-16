package org.webpieces.nio.api.channels;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.webpieces.nio.api.handlers.ChannelSession;


/**
 */
public interface DatagramChannel extends RegisterableChannel
{
    public ChannelSession getSession();    
    
    public void write(SocketAddress addr, ByteBuffer b);
    
}
