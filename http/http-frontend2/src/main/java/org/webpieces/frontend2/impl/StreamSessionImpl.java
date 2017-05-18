package org.webpieces.frontend2.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.webpieces.frontend2.api.StreamSession;

public class StreamSessionImpl implements StreamSession {

	private Map<Object, Object> map = new ConcurrentHashMap<Object, Object>();
	
	public Object get(Object key) {
		return map.get(key);
	}
	
	public void put(Object key, Object value) {
		map.put(key, value);
	}

}
