package com.webpieces.http2parser.api.highlevel;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface ResultListener {

	void incomingPayload(Http2Payload frame);

	CompletableFuture<Void> sendToSocket(ByteBuffer newData);

}
