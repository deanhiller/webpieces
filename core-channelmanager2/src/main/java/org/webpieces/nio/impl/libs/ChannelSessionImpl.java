package org.webpieces.nio.impl.libs;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.libs.ChannelSession;


public class ChannelSessionImpl implements ChannelSession {

	private RegisterableChannel channel;
	private Map<Object, Object> map = new HashMap<Object, Object>();
	
	public ChannelSessionImpl(RegisterableChannel c) {
		this.channel = c;
	}
	
	public RegisterableChannel getChannel() {
		return channel;
	}

	public String toString() {
		return ""+channel;
	}
	
	public Object get(Object key) {
		return map.get(key);
	}
	
	public void put(Object key, Object value) {
		map.put(key, value);
	}
}
