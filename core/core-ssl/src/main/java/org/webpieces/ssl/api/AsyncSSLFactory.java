package org.webpieces.ssl.api;

import javax.net.ssl.SSLEngine;

import org.webpieces.data.api.BufferPool;
import org.webpieces.ssl.impl.AsyncSSLEngine3Impl;
import org.webpieces.ssl.impl.SSLParserImpl;

public class AsyncSSLFactory {

	/**
	 * AsyncSSLEngine is completely stateless in that you can use one engine for multiple ssl sessions.
	 * All state is kept in a memento.  All handshake actions should be single threaded for a 
	 * specific memento except feeding in plain packet can be done at any time after in the connected state.
	 * 
	 * @param pool
	 * @param metrics 
	 * @return
	 */
	public static AsyncSSLEngine create(String loggingId, SSLEngine engine, BufferPool pool, SslListener listener, SSLMetrics metrics) {
		//to get around verifydesign later AND enforce build breaks on design violations
		//like api depending on implementation, we need reflection here to create this
		//instance...
		return new AsyncSSLEngine3Impl(loggingId, engine, pool, listener, metrics);
	}
	
	public static SSLParser create(String logId, SSLEngine engine, BufferPool pool, SSLMetrics metrics) {
		return new SSLParserImpl(logId, engine, pool, metrics);
	}
}
