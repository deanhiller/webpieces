package com.webpieces.http2engine.impl.svr;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.server.ServerEngineListener;
import com.webpieces.http2engine.api.server.StreamReference;
import com.webpieces.http2engine.impl.shared.EngineResultListener;
import com.webpieces.http2engine.impl.shared.Stream;
import com.webpieces.http2parser.api.Http2Exception;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Level8NotifySvrListeners implements EngineResultListener {

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
	public CompletableFuture<Void> sendPieceToApp(Stream stream, PartialStream payload) {
		StreamReference writer = stream.getStreamWriter();
		
		if(payload instanceof Http2Headers && writer == null) {
			writer = listener.sendRequestToServer((Http2Headers) payload, 
					new ResponseHandlerImpl(level1ServerEngine, stream, pushIdGenerator));
			stream.setStreamWriter(writer);
			return CompletableFuture.completedFuture(null);
		}
		
		return writer.sendMore(payload).thenApply((s) -> null);
	}

}
