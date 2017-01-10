package com.webpieces.http2engine.api.client;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;

public interface Http2ClientEngine {

	CompletableFuture<Void> sendInitializationToSocket();

	/**
	 * Future completes one the data is SENT! not when there is a response
	 */
	CompletableFuture<StreamWriter> sendFrameToSocket(Http2Headers headers, Http2ResponseListener responseListener);

	CompletableFuture<Void> sendPing();
	
	void parse(DataWrapper newData);

	/**
	 * completely tear down engine
	 */
	void farEndClosed();
}
