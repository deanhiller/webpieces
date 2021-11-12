package com.webpieces.http2engine.api.server;

import org.webpieces.util.futures.XFuture;

import org.webpieces.data.api.DataWrapper;

public interface Http2ServerEngine {

	XFuture<Void> intialize();

	/**
	 * Usually not used from server-side but could be
	 * @return
	 */
	XFuture<Void> sendPing();
	
	XFuture<Void> parse(DataWrapper newData);
	
	/**
	 * completely tear down engine
	 */
	void farEndClosed();

	void initiateClose(String reason);

}
