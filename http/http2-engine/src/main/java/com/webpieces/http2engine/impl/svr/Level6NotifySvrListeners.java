package com.webpieces.http2engine.impl.svr;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.server.ServerEngineListener;
import com.webpieces.http2engine.impl.shared.EngineResultListener;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class Level6NotifySvrListeners implements EngineResultListener {

	public Level6NotifySvrListeners(ServerEngineListener listener) {
	}

	@Override
	public void sendControlFrameToClient(Http2Msg msg) {
	}

	@Override
	public void farEndClosed() {
	}

	@Override
	public CompletableFuture<Void> sendToSocket(ByteBuffer buffer) {
		return null;
	}

}
