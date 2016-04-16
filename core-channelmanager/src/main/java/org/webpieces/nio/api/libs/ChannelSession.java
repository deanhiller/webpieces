package org.webpieces.nio.api.libs;

import org.webpieces.nio.api.channels.RegisterableChannel;

public interface ChannelSession {

	public RegisterableChannel getChannel();

	public void put(Object key, Object value);
	
	public Object get(Object key);
}
