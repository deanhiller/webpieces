package org.webpieces.frontend2.api;

import org.webpieces.nio.api.SSLEngineFactory;

public interface HttpFrontendManager {

	HttpServer createHttpServer(HttpSvrConfig config, StreamListener listener);
	
	/**
	 * Creates an https server that gives you requests with isHttps set to true.  IF you pass in a 
	 * null SSLEngineFactory, it will open the port as an http port but isHttps will still be set to 
	 * true.  This is specifically for situations where you terminate SSL at the firewall and then do
	 * http internally(I prefer not do that personally).
	 */
	HttpServer createHttpsServer(HttpSvrConfig config, StreamListener listener, SSLEngineFactory factory);

	/**
	 * A special server where the only difference is each request will tell you that it came in on the 
	 * Backend server port.  You can have your backend server be http or https BUT all requests will
	 * have isHttps set to TRUE.  Just pass in null for the SSLEngineFactory and it will 
	 * create an http server
	 */
	HttpServer createBackendHttpsServer(HttpSvrConfig config, StreamListener listener, SSLEngineFactory factory);
	
}
