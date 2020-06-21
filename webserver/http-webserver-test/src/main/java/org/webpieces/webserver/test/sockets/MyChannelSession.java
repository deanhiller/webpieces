package org.webpieces.webserver.test.sockets;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.nio.api.channels.ChannelSession;

public class MyChannelSession implements ChannelSession {

	private Map<Object, Object> map = new HashMap<>();
	
	@Override
	public void put(Object key, Object value) {
		map.put(key, value);
	}

	@Override
	public Object get(Object key) {
		return map.get(key);
	}

}
