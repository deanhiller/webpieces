package org.webpieces.httpclient11.api;

import javax.net.ssl.SSLEngine;

/**
 * You may want to use the Http2to1_1ClientFactory to generate an http2 client that talks http1.1
 * which always allow switching to http2 with a one line change instead of using the 1.1 client
 * The Http2to1_1 code is just a thin translation layer on top of this client
 */
public interface HttpClient {

	/**
	 * HttpSocket is so you can send multiple requests
	 * 
	 * @return
	 */
	public HttpSocket createHttpSocket();
	
	public HttpSocket createHttpsSocket(SSLEngine engine);
	
}
