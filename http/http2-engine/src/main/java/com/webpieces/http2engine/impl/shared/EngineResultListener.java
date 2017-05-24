package com.webpieces.http2engine.impl.shared;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Trailers;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.error.Http2Exception;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public interface EngineResultListener {


	CompletableFuture<Void> sendControlFrameToClient(Http2Msg msg);

	void closeSocket(Http2Exception reason);

	CompletableFuture<Void> sendToSocket(ByteBuffer buffer);

	CompletableFuture<Void> sendRstToApp(Stream stream, RstStreamFrame payload);

	CompletableFuture<Void> sendPieceToApp(Stream stream, PartialStream payload);
	
	CompletableFuture<Void> sendPieceToApp(Stream stream, Http2Trailers payload);
	
}
