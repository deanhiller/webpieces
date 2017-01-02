package com.webpieces.http2engine.api;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.dto.PartialStream;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public interface ResultListener {

	void incomingPayload(PartialStream frame);
	
	void incomingControlFrame(Http2Frame lowLevelFrame);

	CompletableFuture<Void> sendToSocket(ByteBuffer newData);

	void engineClosed();

}
