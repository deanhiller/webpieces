package org.webpieces.asyncserver.api;

import org.webpieces.nio.api.SSLEngineFactory;

public interface AsyncServerManager {

	AsyncServer createTcpServer(AsyncConfig config, AsyncDataListener listener);

	AsyncServer createTcpServer(AsyncConfig config, AsyncDataListener listener, SSLEngineFactory sslFactory);

	public String getName();
}
