package org.webpieces.asyncserver.api;

import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.handlers.AsyncDataListener;

public interface AsyncServerManager {

	AsyncServer createTcpServer(AsyncConfig config, AsyncDataListener listener);

	AsyncServer createTcpServer(AsyncConfig config, AsyncDataListener listener, SSLEngineFactory sslFactory);
	
}
