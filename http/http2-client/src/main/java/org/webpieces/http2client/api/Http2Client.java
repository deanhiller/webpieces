package org.webpieces.http2client.api;

import javax.net.ssl.SSLEngine;

public interface Http2Client {

	/**
	 * HttpClientSocket is so you can send multiple requests
	 * 
	 * @return
	 */
	public Http2Socket createHttpSocket();

	/**
	 * HttpClientSocket is so you can send multiple requests.
	 * 
	 * @return
	 */
	public Http2Socket createHttpsSocket(SSLEngine factory);
	
}
