package org.webpieces.httpfrontend2.api.adaptor;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServerChannels {
	private Map<SocketAddress, AdaptorServerChannel> serverChannels = new HashMap<>();

	void bindTo(SocketAddress addr, AdaptorServerChannel channel) {
		serverChannels.put(addr, channel);
	}
	
	AdaptorServerChannel lookup(SocketAddress addr) {
		return serverChannels.get(addr);
	}
}
