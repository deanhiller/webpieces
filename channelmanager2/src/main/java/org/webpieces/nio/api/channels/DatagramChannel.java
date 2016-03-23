package org.webpieces.nio.api.channels;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.webpieces.nio.api.handlers.ChannelSession;
import org.webpieces.nio.api.handlers.DatagramListener;


/**
 */
public interface DatagramChannel extends RegisterableChannel
{
    public void registerForReads(DatagramListener listener);
    
    public void unregisterForReads();
    
    public ChannelSession getSession();    
    
    public void write(SocketAddress addr, ByteBuffer b);
    
}
