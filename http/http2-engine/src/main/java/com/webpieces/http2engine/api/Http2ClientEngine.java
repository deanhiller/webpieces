package com.webpieces.http2engine.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2engine.api.dto.PartialStream;

public interface Http2ClientEngine {

	CompletableFuture<Void> sendInitializationToSocket();

	CompletableFuture<Void> sendFrameToSocket(PartialStream frame);

	void cancel(int streamId);

	void parse(DataWrapper newData);

	/**
	 * completely tear down engine
	 */
	void closeEngine();

}
