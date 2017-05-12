package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.server.ResponseHandler;
import com.webpieces.http2engine.impl.shared.Stream;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class ResponseHandlerImpl implements ResponseHandler {

	private Level1ServerEngine level1ServerEngine;
	private Stream stream;
	private AtomicInteger pushIdGenerator;

	public ResponseHandlerImpl(Level1ServerEngine level1ServerEngine, Stream stream, AtomicInteger idGenerator) {
		this.level1ServerEngine = level1ServerEngine;
		this.stream = stream;
		this.pushIdGenerator = idGenerator;
	}

	@Override
	public CompletableFuture<StreamWriter> sendResponse(Http2Headers headerPiece) {
		headerPiece.setStreamId(stream.getStreamId());
		return level1ServerEngine.sendResponseHeaders(stream, headerPiece);
	}

	@Override
	public CompletableFuture<StreamWriter> sendPush(Http2Push push) {
		return null;
	}

	@Override
	public void cancelStream() {
	}

}
