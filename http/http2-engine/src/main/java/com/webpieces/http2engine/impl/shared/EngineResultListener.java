package com.webpieces.http2engine.impl.shared;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public interface EngineResultListener {


	void sendControlFrameToClient(Http2Msg msg);

	void farEndClosed();

	CompletableFuture<Void> sendToSocket(ByteBuffer buffer);


}
