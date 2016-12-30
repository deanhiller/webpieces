package com.webpieces.http2engine.api;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface ResultListener {

	void incomingPayload(Http2Payload frame, boolean isTrailingHeaders);

	CompletableFuture<Void> sendToSocket(ByteBuffer newData);

}
