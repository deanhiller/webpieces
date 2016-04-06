package org.webpieces.httpproxy.impl.chain;

import java.util.List;

import javax.inject.Inject;

import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.Channel;

import com.webpieces.httpparser.api.dto.HttpRequest;

public class Layer4Processor {

	@Inject
	private ChannelManager channelManager;
	
	public void processHttpRequests(Channel channel, List<HttpRequest> parsedRequests) {
		channelManager.createTCPChannel("outbound");
	}

}
