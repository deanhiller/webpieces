package org.webpieces.nio.api.handlers;


import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;


public interface ConnectionListener {
	
	public void connected(Channel channel);
	
	public void failed(RegisterableChannel channel, Throwable e);
}
