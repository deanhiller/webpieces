package com.webpieces.http2engine.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;

public interface Http2ClientEngine {

	CompletableFuture<Void> sendInitializationToSocket();

	CompletableFuture<Void> sendFrameToSocket(Http2Payload frame);

	void cancel(int streamId);

	void parse(DataWrapper newData);


}
