package com.webpieces.http2engine.impl.svr;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.server.ServerEngineListener;
import com.webpieces.http2engine.impl.shared.EngineResultListener;
import com.webpieces.http2engine.impl.shared.Stream;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Level6NotifySvrListeners implements EngineResultListener {

	private ServerEngineListener listener;

	public Level6NotifySvrListeners(ServerEngineListener listener) {
		this.listener = listener;
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
	public void farEndClosed() {
	}

	@Override
	public CompletableFuture<Void> sendToSocket(ByteBuffer buffer) {
		return listener.sendToSocket(buffer).thenApply(s -> null);
	}

	@Override
	public CompletableFuture<Void> sendPieceToClient(Stream stream, PartialStream payload) {
		return null;
	}

}
