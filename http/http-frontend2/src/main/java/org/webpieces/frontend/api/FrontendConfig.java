package org.webpieces.frontend.api;

import java.net.SocketAddress;

import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.data.api.BufferCreationPool;

import com.webpieces.http2engine.impl.shared.HeaderSettings;

public class FrontendConfig {

	public AsyncConfig asyncServerConfig = new AsyncConfig();
	
	public SocketAddress bindAddress;
	
	/**
	 * When a client connects, they must send a request in this amount of time.  null means disabled.
	 * telnet google.com 443 yeilds an 11 second timeout while telnet google.com 80 yeilds a 
	 * 60 second timeout so wait until 65 seconds 
	 */
	public Integer maxConnectToRequestTimeoutMs = 65000;
	
	/**
	 * null means keep alive will be disabled
	 */
	public Integer keepAliveTimeoutMs = 15000;
	
	/**
	 * The max size a client may send.  I advise not too large a limit here or DOS attacks become easier in that
	 * you can send very large request to eat up memory of the server.
	 * 
	 * This is the max size of any http request(headers that is) or any chunk being uploaded.
	 *
	 */
	public int maxHeaderSize = 4096;
	
	public int maxBodyOrChunkSize = BufferCreationPool.DEFAULT_MAX_BUFFER_SIZE;

	public HeaderSettings localSettings = new HeaderSettings();

	/**
	 * This turns on HTTP/2 by default, so HTTP/1.1 won't work. Only needed for testing with
	 * 'h2spec' over cleartext. Will be made obsolete once we have ALPN.
	 */
	public boolean alwaysHttp2 = false;

	public FrontendConfig(String id, SocketAddress bindAddr) {
		asyncServerConfig.id = id;
		this.bindAddress = bindAddr;
	}
	
	public FrontendConfig(String id, SocketAddress bindAddr, Integer connectToRequestTimeout) {
		asyncServerConfig.id = id;
		this.bindAddress = bindAddr;
		maxConnectToRequestTimeoutMs = connectToRequestTimeout;
	}

}
