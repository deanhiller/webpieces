package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.http2engine.api.server.Http2ServerEngine;
import com.webpieces.http2engine.api.server.ServerEngineListener;

public class Level1ServerEngine implements Http2ServerEngine {

	private HpackParser http2Parser;
	private ServerEngineListener listener;

	public Level1ServerEngine(HpackParser http2Parser, ServerEngineListener listener) {
		this.http2Parser = http2Parser;
		this.listener = listener;
	}

	@Override
	public CompletableFuture<Void> sendPing() {
		return null;
	}

	@Override
	public void parse(DataWrapper newData) {
	}

	@Override
	public void farEndClosed() {
	}

	@Override
	public void initiateClose() {
	}
}
