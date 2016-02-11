package org.playorm.nio.api.mgmt;

import java.net.InetSocketAddress;

public interface RegisterableChannelMBean {
	
	public String getId();
	
	public String toString();
	
	public boolean isBlocking();
    
	/**
	 * Closes and unregisters the channel if registered from the ChannelManager
	 */
	public void close();

	public boolean isClosed();
	
	/**
	 * @return true if the Channel is bound, and false otherwise.
	 */
	public boolean isBound();
	
	/**
	 * @return the local InetSocketAddress
	 */
	public InetSocketAddress getLocalAddress();	
}
