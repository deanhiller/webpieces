package org.webpieces.httpclient.api.mocks;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.nio.api.channels.ChannelSession;

public class MockSession implements ChannelSession {

	private Map<Object, Object> map = new HashMap<>();
	
	@Override
	public void put(Object key, Object value) {
		this.map.put(key, value);
	}

	@Override
	public Object get(Object key) {
		return map.get(key);
	}

}
