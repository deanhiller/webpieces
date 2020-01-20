package org.webpieces.frontend2.api;

import java.net.InetSocketAddress;

import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.data.api.BufferCreationPool;

public class HttpSvrConfig {

	public AsyncConfig asyncServerConfig = new AsyncConfig();
	
	public InetSocketAddress bindAddress;
		
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

	public HttpSvrConfig(String id) {
		this(id, new InetSocketAddress(0));
	}
	
	public HttpSvrConfig(String id, InetSocketAddress bindAddr) {
		asyncServerConfig.id = id;
		this.bindAddress = bindAddr;
	}
	
	public HttpSvrConfig(String id, InetSocketAddress bindAddress, Integer connectToRequestTimeout) {
		asyncServerConfig.id = id;
		this.bindAddress = bindAddress;
		maxConnectToRequestTimeoutMs = connectToRequestTimeout;
	}

}
