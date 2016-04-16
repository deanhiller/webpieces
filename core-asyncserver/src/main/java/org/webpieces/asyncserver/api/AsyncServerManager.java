package org.webpieces.asyncserver.api;

import java.net.SocketAddress;

import org.webpieces.nio.api.handlers.DataListener;

public interface AsyncServerManager {

	public AsyncServer createTcpServer(String id, SocketAddress addr, DataListener listener);
	
}
