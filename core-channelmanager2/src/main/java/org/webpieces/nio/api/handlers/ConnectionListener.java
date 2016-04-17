package org.webpieces.nio.api.handlers;


import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;


public interface ConnectionListener {
	
	public void connected(Channel channel);
	
	/**
	 * Unfortunately, channel may be the TCPServerChannel if accepting and failed or
	 * the TCPChannel when finishConnecting fails.  If doing UDP, it would be the 
	 * UDPChannel every time.
	 * 
	 */
	public void failed(RegisterableChannel channel, Throwable e);
}
