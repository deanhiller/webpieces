package org.playorm.nio.api.libs;

import org.playorm.nio.api.channels.RegisterableChannel;

public interface ChannelSession {

	public RegisterableChannel getChannel();

	public void put(Object key, Object value);
	
	public Object get(Object key);
}
