package com.webpieces.http2engine.api.client;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.hpack.api.dto.Http2Headers;

public interface Http2ClientEngine {

	CompletableFuture<Void> sendInitializationToSocket();

	/**
	 * Future completes one the data is SENT! not when there is a response
	 */
	CompletableFuture<ClientStreamWriter> sendFrameToSocket(Http2Headers headers, Http2ResponseListener responseListener);

	CompletableFuture<Void> sendPing();
	
	void parse(DataWrapper newData);

	/**
	 * completely tear down engine
	 */
	void farEndClosed();

	void initiateClose(String reason);
}
