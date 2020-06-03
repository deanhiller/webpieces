package com.webpieces.http2engine.impl.shared;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2.api.dto.highlevel.Http2Trailers;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2engine.api.error.ShutdownConnection;
import com.webpieces.http2engine.impl.shared.data.Stream;

public interface EngineResultListener {


	CompletableFuture<Void> sendControlFrameToClient(Http2Msg msg);

	void closeSocket(ShutdownConnection shutdown);

	CompletableFuture<Void> sendToSocket(ByteBuffer buffer);

	CompletableFuture<Void> sendRstToApp(Stream stream, CancelReason payload);

	CompletableFuture<Void> sendPieceToApp(Stream stream, StreamMsg payload);
	
	CompletableFuture<Void> sendPieceToApp(Stream stream, Http2Trailers payload);
	
}
