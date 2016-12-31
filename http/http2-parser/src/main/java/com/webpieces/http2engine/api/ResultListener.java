package com.webpieces.http2engine.api;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.dto.Http2Frame;

public interface ResultListener {

	void incomingPayload(Http2Payload frame, boolean isTrailingHeaders);

	CompletableFuture<Void> sendToSocket(ByteBuffer newData);

	void incomingControlFrame(Http2Frame lowLevelFrame);

	void engineClosed();

}
