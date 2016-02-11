package org.playorm.nio.api.channels;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.playorm.nio.api.handlers.DatagramListener;
import org.playorm.nio.api.libs.ChannelSession;


/**
 */
public interface DatagramChannel extends RegisterableChannel
{
    public void registerForReads(DatagramListener listener);
    
    public void unregisterForReads();
    
    public ChannelSession getSession();    
    
    public void oldWrite(SocketAddress addr, ByteBuffer b);
    
	/**
	 * Closes and unregisters the channel if registered from the ChannelManager
	 */
	public void close();
}
