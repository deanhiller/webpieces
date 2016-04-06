package org.webpieces.httpproxy.api;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;

public class MockChannelManager implements ChannelManager {

	private List<ConnectionListener> serverListeners = new ArrayList<>();
	
	@Override
	public TCPServerChannel createTCPServerChannel(String id) {
		return null;
	}

	@Override
	public TCPServerChannel createTCPServerChannel(String string, SocketAddress addr,
			ConnectionListener serverListener) {
		serverListeners.add(serverListener);
		return null;
	}

	@Override
	public TCPChannel createTCPChannel(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UDPChannel createUDPChannel(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DatagramChannel createDatagramChannel(String id, int bufferSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	public List<ConnectionListener> getServerListeners() {
		return serverListeners;
	}

}
