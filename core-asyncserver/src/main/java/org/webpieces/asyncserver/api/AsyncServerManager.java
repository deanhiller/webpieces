package org.webpieces.asyncserver.api;

import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.handlers.DataListener;

public interface AsyncServerManager {

	AsyncServer createTcpServer(AsyncConfig config, DataListener listener);

	AsyncServer createTcpServer(AsyncConfig config, DataListener listener, SSLEngineFactory sslFactory);
	
}
