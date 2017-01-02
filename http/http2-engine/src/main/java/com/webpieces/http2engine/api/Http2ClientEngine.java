package com.webpieces.http2engine.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2engine.api.dto.Http2Headers;

public interface Http2ClientEngine {

	CompletableFuture<Void> sendInitializationToSocket();
	CompletableFuture<RequestWriter> sendFrameToSocket(Http2Headers headers, Http2ResponseListener responseListener);

	CompletableFuture<Void> sendPing();
	
	
	void parse(DataWrapper newData);

	/**
	 * completely tear down engine
	 */
	void closeEngine();
}
