package org.playorm.nio.api.handlers;


import java.io.IOException;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;


public interface ConnectionListener {
	
	public void connected(Channel channel) throws IOException;
	
	public void failed(RegisterableChannel channel, Throwable e);
}
