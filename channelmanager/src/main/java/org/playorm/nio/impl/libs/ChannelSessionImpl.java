package org.playorm.nio.impl.libs;

import java.util.HashMap;
import java.util.Map;

import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.libs.ChannelSession;


public class ChannelSessionImpl implements ChannelSession {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
