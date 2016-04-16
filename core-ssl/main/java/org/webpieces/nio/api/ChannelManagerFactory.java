package org.webpieces.nio.api;

import java.util.Map;


public class ChannelManagerFactory {

	private ChannelManagerFactory() {}
	
	public static ChannelManager createChannelManager(String id, Map<String, Object> props) {
		//TODO: we must reflect here to avoid design issues
		return null;
	}
}
