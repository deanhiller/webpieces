package org.webpieces.httpproxy.api;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.handlers.DataListener;

public class MockAsyncServerManager implements AsyncServerManager {

	private List<DataListener> serverListeners = new ArrayList<>();


	public List<DataListener> getServerListeners() {
		return serverListeners;
	}

	@Override
	public TCPServerChannel createTcpServer(String id, SocketAddress addr, DataListener listener) {
		serverListeners.add(listener);
		return null;
	}

}
