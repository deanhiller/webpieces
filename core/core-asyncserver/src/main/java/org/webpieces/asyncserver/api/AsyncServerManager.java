package org.webpieces.asyncserver.api;

import org.webpieces.nio.api.SSLEngineFactory;

public interface AsyncServerManager {

	AsyncServer createTcpServer(AsyncConfig config, AsyncDataListener listener);

	AsyncServer createTcpServer(AsyncConfig config, AsyncDataListener listener, SSLEngineFactory sslFactory);

	/**
	 * Creates a TCP Socket that can receive SSL OR plain text traffic
	 */
	AsyncServer createUpgradableServer(AsyncConfig asyncServerConfig, AsyncDataListener listener,
			SSLEngineFactory factory);
	
	public String getName();


}
