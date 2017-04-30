package org.webpieces.http2client.api;

import javax.net.ssl.SSLEngine;

public interface Http2Client {

	/**
	 * HttpClientSocket is so you can send multiple requests
	 * 
	 * @return
	 */
	public Http2Socket createHttpSocket(String idForLogging);

	/**
	 * HttpClientSocket is so you can send multiple requests.
	 * 
	 * @return
	 */
	public Http2Socket createHttpsSocket(String idForLogging, SSLEngine factory);
	
}
