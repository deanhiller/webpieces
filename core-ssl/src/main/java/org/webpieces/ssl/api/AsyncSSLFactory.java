package org.webpieces.ssl.api;

import javax.net.ssl.SSLEngine;

import org.webpieces.ssl.impl.AsyncSSLEngine2Impl;

import com.webpieces.data.api.BufferPool;

public class AsyncSSLFactory {

	/**
	 * AsyncSSLEngine is completely stateless in that you can use one engine for multiple ssl sessions.
	 * All state is kept in a memento.  All handshake actions should be single threaded for a 
	 * specific memento except feeding in plain packet can be done at any time after in the connected state.
	 * 
	 * @param pool
	 * @return
	 */
	public static AsyncSSLEngine createParser(String loggingId, SSLEngine engine, BufferPool pool, SslListener listener) {
		//to get around verifydesign later AND enforce build breaks on design violations
		//like api depending on implementation, we need reflection here to create this
		//instance...
		return new AsyncSSLEngine2Impl(loggingId, engine, pool, listener);
	}
}
