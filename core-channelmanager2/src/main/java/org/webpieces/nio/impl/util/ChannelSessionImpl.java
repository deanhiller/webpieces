package org.webpieces.nio.impl.util;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.nio.api.channels.ChannelSession;


public class ChannelSessionImpl implements ChannelSession {

	private Map<Object, Object> map = new HashMap<Object, Object>();
	
	public Object get(Object key) {
		return map.get(key);
	}
	
	public void put(Object key, Object value) {
		map.put(key, value);
	}

}
