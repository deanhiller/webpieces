package org.webpieces.httpfrontend2.api.adaptor;

import javax.net.ssl.SSLEngine;

import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DatagramListener;

public class AdaptorChannelManager implements ChannelManager {

	private ServerChannels svrChannels = new ServerChannels();

	@Override
	public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connectionListener) {
		return new AdaptorServerChannel(id, connectionListener, svrChannels);
	}

	@Override
	public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connectionListener,
			SSLEngineFactory factory) {
		return null;
	}

	@Override
	public TCPChannel createTCPChannel(String id) {
		return new ClientChannel(id, svrChannels);
	}

	@Override
	public TCPChannel createTCPChannel(String id, SSLEngine engine) {
		return null;
	}

	@Override
	public UDPChannel createUDPChannel(String id) {
		return null;
	}

	@Override
	public DatagramChannel createDatagramChannel(String id, int bufferSize, DatagramListener listener) {
		return null;
	}

	@Override
	public void stop() {
	}
}
