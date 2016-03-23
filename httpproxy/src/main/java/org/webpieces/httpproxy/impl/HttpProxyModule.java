package org.webpieces.httpproxy.impl;

import javax.inject.Singleton;

import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;

public class HttpProxyModule implements Module {

	@Override
	public void configure(Binder binder) {

	}

	@Provides
	@Singleton
	public ChannelManager providesChannelManager() {
		return ChannelManagerFactory.createChannelManager("chanMgr", null);
	}
	
	@Provides
	public TCPServerChannel provideServerChannel(ChannelManager manager) {
		return manager.createTCPServerChannel("httpServerChannel");
	}
}
