package org.webpieces.httpclient.api2;

import javax.net.ssl.SSLEngine;

public interface HttpClient {

	/**
	 * HttpClientSocket is so you can send multiple requests AND if you are doing http/2, it will turn
	 * off server push_promises as well so the server cannot pre-emptively send you responses.  For
	 * interacting with apis, this is fine.
	 * 
	 * @return
	 */
	public HttpSocket createHttpSocket(String idForLogging);

	/**
	 * HttpClientSocket is so you can send multiple requests AND if you are doing http/2, it will turn
	 * off server push_promises as well so the server cannot pre-emptively send you responses.  For
	 * interacting with apis, this is fine.
	 * 
	 * @return
	 */
	public HttpSocket createHttpsSocket(String idForLogging, SSLEngine factory);
	
}