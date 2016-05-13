package org.webpieces.asyncserver.api;

import java.net.SocketAddress;

import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.handlers.DataListener;

public interface AsyncServerManager {

	AsyncServer createTcpServer(String id, SocketAddress addr, DataListener listener);

	AsyncServer createTcpServer(String id, SocketAddress addr, DataListener listener, SSLEngineFactory sslFactory);
	
}
