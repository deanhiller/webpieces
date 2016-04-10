package org.webpieces.asyncserver.impl;

import java.net.SocketAddress;

import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.handlers.DataListener;

public class AsyncServerManagerImpl implements AsyncServerManager {

	@Override
	public TCPServerChannel createTcpServer(String id, SocketAddress addr, DataListener listener) {
		return null;
	}

}
