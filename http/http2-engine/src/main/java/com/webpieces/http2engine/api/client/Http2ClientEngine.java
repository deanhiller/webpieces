package com.webpieces.http2engine.api.client;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2.api.streaming.RequestStreamHandle;

public interface Http2ClientEngine {

	CompletableFuture<Void> sendInitializationToSocket();

	/**
	 * Future completes one the data is SENT! not when there is a response which allows to backpressure the socket and
	 * deregister from reading if clients do not keep up
	 */
	RequestStreamHandle openStream();

	CompletableFuture<Void> sendPing();
	
	CompletableFuture<Void> parse(DataWrapper newData);

	/**
	 * completely tear down engine
	 */
	void farEndClosed();

	void initiateClose(String reason);

}
