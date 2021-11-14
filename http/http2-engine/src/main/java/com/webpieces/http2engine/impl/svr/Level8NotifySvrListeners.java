package com.webpieces.http2engine.impl.svr;

import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Trailers;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.RequestStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;
import com.webpieces.http2engine.api.error.ShutdownConnection;
import com.webpieces.http2engine.api.server.ServerEngineListener;
import com.webpieces.http2engine.impl.shared.EngineResultListener;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class Level8NotifySvrListeners implements EngineResultListener {

	//private static final Logger log = LoggerFactory.getLogger(Level8NotifySvrListeners.class);

	private ServerEngineListener listener;
	private Level1ServerEngine level1ServerEngine;
	private AtomicInteger pushIdGenerator = new AtomicInteger(2);

	public Level8NotifySvrListeners(ServerEngineListener listener, Level1ServerEngine level1ServerEngine) {
		this.listener = listener;
		this.level1ServerEngine = level1ServerEngine;
	}

	@Override
	public XFuture<Void> sendControlFrameToClient(Http2Msg msg) {
		XFuture<Void> future = new XFuture<Void>();
		try {
			future.complete(null);
		} catch(Throwable e) {
			future.completeExceptionally(new RuntimeException(e));
		}
		return future;
	}

	@Override
	public void closeSocket(ShutdownConnection reason) {
		listener.closeSocket(reason);
	}
	
	@Override
	public XFuture<Void> sendToSocket(ByteBuffer buffer) {
		return listener.sendToSocket(buffer).thenApply(s -> null);
	}

	@Override
	public XFuture<Void> sendRstToApp(Stream stream, CancelReason payload) {
		if(stream instanceof ServerStream) {
			ServerStream str = (ServerStream) stream;
			StreamRef streamRef = str.getStreamRef();
			return streamRef.cancel(payload);
		}

		//since the stream is closed, any writes to the push streams will automatically close and be cancelled
		return XFuture.completedFuture(null);
	}
	
	@Override
	public XFuture<Void> sendPieceToApp(Stream stream, StreamMsg payload) {
		ServerStream s = (ServerStream) stream;
		XFuture<StreamWriter> writer = s.getStreamWriter();
	
		return writer.thenCompose(w -> w.processPiece(payload));
	}

	@Override
	public XFuture<Void> sendPieceToApp(Stream stream, Http2Trailers payload) {
		ServerStream s = (ServerStream) stream;
		XFuture<StreamWriter> writer = s.getStreamWriter();
		return writer.thenCompose(w -> w.processPiece(payload));
	}

	public XFuture<Void> fireRequestToApp(ServerStream stream, Http2Request payload) {
		SvrSideResponseHandler handler = new SvrSideResponseHandler(level1ServerEngine, stream, pushIdGenerator);
		RequestStreamHandle streamHandle = listener.openStream();
		StreamRef streamRef = streamHandle.process(payload, handler);
		stream.setStreamHandle(streamHandle, streamRef);

		return streamRef.getWriter().thenApply(w -> null);
	}

}
