package org.webpieces.webserver.test;

import javax.net.ssl.SSLEngine;

import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DatagramListener;

public class MockChannelManager implements ChannelManager {

	private ConnectionListener httpConnectionListener;
	private ConnectionListener httpsConnectionListener;
	private ConnectionListener backendConnectionListener;

	@Override
	public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connectionListener) {
		if(id.startsWith("backend")) {
			backendConnectionListener = connectionListener;
			return new MockServerChannel();
		}
		httpConnectionListener = connectionListener;
		return new MockServerChannel();
	}

	@Override
	public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connectionListener,
			SSLEngineFactory factory) {
		if(id.startsWith("backend")) {
			backendConnectionListener = connectionListener;
			return new MockServerChannel();
		}
		httpsConnectionListener = connectionListener;
		return new MockServerChannel();
	}

	@Override
	public TCPChannel createTCPChannel(String id) {
		return null;
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

	public ConnectionListener getHttpConnection() {
		return httpConnectionListener;
	}

	public ConnectionListener getHttpsConnection() {
		return httpsConnectionListener;
	}
	
	public ConnectionListener getBackendConnection() {
		return backendConnectionListener;
	}
}
