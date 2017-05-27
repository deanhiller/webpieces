package com.webpieces.http2engine.impl.shared;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Trailers;
import com.webpieces.http2engine.api.error.ShutdownConnection;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public interface EngineResultListener {


	CompletableFuture<Void> sendControlFrameToClient(Http2Msg msg);

	void closeSocket(ShutdownConnection shutdown);

	CompletableFuture<Void> sendToSocket(ByteBuffer buffer);

	CompletableFuture<Void> sendRstToApp(Stream stream, CancelReason payload);

	CompletableFuture<Void> sendPieceToApp(Stream stream, StreamMsg payload);
	
	CompletableFuture<Void> sendPieceToApp(Stream stream, Http2Trailers payload);
	
}
