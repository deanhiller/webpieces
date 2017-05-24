package com.webpieces.http2engine.impl.svr;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Trailers;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.server.ServerEngineListener;
import com.webpieces.http2engine.impl.shared.EngineResultListener;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.error.Http2Exception;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

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
	public CompletableFuture<Void> sendControlFrameToClient(Http2Msg msg) {
		CompletableFuture<Void> future = new CompletableFuture<Void>();
		try {
			future.complete(null);
		} catch(Throwable e) {
			future.completeExceptionally(new RuntimeException(e));
		}
		return future;
	}

	@Override
	public void closeSocket(Http2Exception reason) {
		listener.closeSocket(reason);
	}
	
	@Override
	public CompletableFuture<Void> sendToSocket(ByteBuffer buffer) {
		return listener.sendToSocket(buffer).thenApply(s -> null);
	}

	@Override
	public CompletableFuture<Void> sendRstToApp(Stream stream, RstStreamFrame payload) {
		if(stream instanceof ServerStream) {
			ServerStream str = (ServerStream) stream;
			StreamHandle handle = str.getStreamHandle();
			return handle.cancel(payload);
		}

		//since the stream is closed, any writes to the push streams will automatically close and be cancelled
		return CompletableFuture.completedFuture(null);
	}
	
	@Override
	public CompletableFuture<Void> sendPieceToApp(Stream stream, PartialStream payload) {
		ServerStream s = (ServerStream) stream;
		StreamWriter writer = s.getStreamWriter();
		return writer.processPiece(payload).thenApply(v -> null);
	}

	@Override
	public CompletableFuture<Void> sendPieceToApp(Stream stream, Http2Trailers payload) {
		ServerStream s = (ServerStream) stream;
		StreamWriter writer = s.getStreamWriter();
		return writer.processPiece(payload).thenApply(w->null);
	}

	public CompletableFuture<Void> fireRequestToApp(ServerStream stream, Http2Request payload) {
		ResponseHandlerImpl handler = new ResponseHandlerImpl(level1ServerEngine, stream, pushIdGenerator);
		StreamHandle streamHandle = listener.openStream(payload.getStreamId(), handler);
		stream.setStreamHandle(streamHandle);
		return streamHandle.process(payload)
				.thenApply( w -> {
					stream.setStreamWriter(w);				
					return null;
				});
	}

}
