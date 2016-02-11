package org.playorm.nio.api.mgmt;

import java.net.SocketAddress;


public interface ChannelMBean extends RegisterableChannelMBean {
	/**
	 * @return the remote SocketAddress
	 */
	public SocketAddress getRemoteAddress();
	
	public boolean isConnected();
	
	//would be a nice addition!!!
	//public boolean isRegisteredForReads();
}
