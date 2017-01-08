package com.webpieces.http2engine.api;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public interface EngineListener {

	void sendControlFrameToClient(Http2Frame lowLevelFrame);

	CompletableFuture<Void> sendToSocket(ByteBuffer newData);

	void engineClosedByFarEnd();

}
