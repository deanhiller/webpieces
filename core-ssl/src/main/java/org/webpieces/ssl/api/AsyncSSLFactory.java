package org.webpieces.ssl.api;

import org.webpieces.ssl.impl.AsyncSSLEngine2Impl;

import com.webpieces.data.api.BufferPool;

public class AsyncSSLFactory {

	public static AsyncSSLEngine createParser(BufferPool pool) {
		//to get around verifydesign later AND enforce build breaks on design violations
		//like api depending on implementation, we need reflection here to create this
		//instance...
		return new AsyncSSLEngine2Impl(pool);
	}
}
