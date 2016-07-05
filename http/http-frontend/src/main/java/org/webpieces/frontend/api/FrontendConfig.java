package org.webpieces.frontend.api;

import java.net.SocketAddress;

import org.webpieces.asyncserver.api.AsyncConfig;

public class FrontendConfig {

	public AsyncConfig asyncServerConfig = new AsyncConfig();
	
	/**
	 * When a client connects, they must send a request in this amount of timer.  null means disabled.
	 */
	public Integer maxConnectToRequestTimeoutMs = 4000;
	
	/**
	 * null means keep alive will be disabled
	 */
	public Integer keepAliveTimeoutMs = 15000;
	
	/**
	 * The max size a client may send.  I advise not too large a limit here or DOS attacks become easier in that
	 * you can send very large request to eat up memory of the server.
	 * 
	 * This is the max size of any http request(headers that is) or any chunk being uploaded
	 */
	public int maxPayloadSize = 16384;

	public FrontendConfig(String id, SocketAddress bindAddr) {
		asyncServerConfig.id = id;
		asyncServerConfig.bindAddr = bindAddr;
	}
}
