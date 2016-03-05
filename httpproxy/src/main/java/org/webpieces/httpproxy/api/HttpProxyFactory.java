package org.webpieces.httpproxy.api;

import java.util.Map;

import org.webpieces.nio.api.ChannelManager;

public class HttpProxyFactory {
	private HttpProxyFactory() {}
	
	public static HttpProxy createHttpProxy(String id, Map<String, Object> props) {
		//TODO: we must reflect here to avoid design issues
		return null;
	}
	
}
