package org.webpieces.httpclient.api;

import javax.net.ssl.SSLEngine;

public interface HttpClient {

	/**
	 * HttpSocket is so you can send multiple requests
	 * 
	 * @return
	 */
	public HttpSocket createHttpSocket(String idForLogging);
	
	public HttpSocket createHttpsSocket(String idForLogging, SSLEngine engine);
	
}
