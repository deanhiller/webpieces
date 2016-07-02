package org.webpieces.nio.api.channels;

public interface ChannelSession {

	public void put(Object key, Object value);
	
	public Object get(Object key);
}
