package org.webpieces.nio.api.channels;

import java.net.SocketAddress;
import java.nio.ByteBuffer;


/**
 */
public interface DatagramChannel extends RegisterableChannel
{
    public void registerForReads();
    
    public void unregisterForReads();
    
    public ChannelSession getSession();    
    
    public void write(SocketAddress addr, ByteBuffer b);
    
	/**
	 * Closes and unregisters the channel if registered from the ChannelManager
	 */
	public void close();
}
