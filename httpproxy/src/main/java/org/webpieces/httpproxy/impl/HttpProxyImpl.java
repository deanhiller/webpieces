package org.webpieces.httpproxy.impl;

import javax.inject.Inject;

import org.webpieces.httpproxy.api.HttpProxy;
import org.webpieces.nio.api.ChannelManager;

public class HttpProxyImpl implements HttpProxy {

	@Inject
	private ChannelManager channelManager;
	
	@Override
	public void start() {
		//channelManager.createTCPServerChannel(")
		
	}

	@Override
	public void stop() {
		
	}

}
