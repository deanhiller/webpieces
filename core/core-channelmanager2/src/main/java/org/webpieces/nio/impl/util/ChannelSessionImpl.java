package org.webpieces.nio.impl.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.webpieces.nio.api.channels.ChannelSession;


public class ChannelSessionImpl implements ChannelSession {

	private Map<Object, Object> map = new ConcurrentHashMap<Object, Object>();
	
	public Object get(Object key) {
		return map.get(key);
	}
	
	public void put(Object key, Object value) {
		map.put(key, value);
	}

}
