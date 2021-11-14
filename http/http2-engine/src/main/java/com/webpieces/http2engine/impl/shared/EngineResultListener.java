package com.webpieces.http2engine.impl.shared;

import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.highlevel.Http2Trailers;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2engine.api.error.ShutdownConnection;
import com.webpieces.http2engine.impl.shared.data.Stream;

public interface EngineResultListener {


	XFuture<Void> sendControlFrameToClient(Http2Msg msg);

	void closeSocket(ShutdownConnection shutdown);

	XFuture<Void> sendToSocket(ByteBuffer buffer);

	XFuture<Void> sendRstToApp(Stream stream, CancelReason payload);

	XFuture<Void> sendPieceToApp(Stream stream, StreamMsg payload);
	
	XFuture<Void> sendPieceToApp(Stream stream, Http2Trailers payload);
	
}
