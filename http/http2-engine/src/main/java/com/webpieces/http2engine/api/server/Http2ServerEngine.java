package com.webpieces.http2engine.api.server;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;

public interface Http2ServerEngine {

	/**
	 * Usually not used from server-side but could be
	 * @return
	 */
	CompletableFuture<Void> sendPing();
	
	void parse(DataWrapper newData);
	
	/**
	 * completely tear down engine
	 */
	void farEndClosed();

	void initiateClose();
}
