package com.webpieces.http2engine.api.client;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.Http2Exception;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public interface ClientEngineListener {

	void sendControlFrameToClient(Http2Frame lowLevelFrame);

	CompletableFuture<Void> sendToSocket(ByteBuffer newData);

	void engineClosedByFarEnd();

	void closeSocket(Http2Exception reason);

}
