package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.server.ResponseHandler;
import com.webpieces.http2engine.impl.shared.Stream;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;

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
		if(headerPiece.getStreamId() != 0 && headerPiece.getStreamId() != stream.getStreamId())
			throw new IllegalArgumentException("WE WILL SET the Http2Headers.streamID so you should leave it as 0 or the right id.  stream="+stream.getStreamId()+" frame="+headerPiece);
		headerPiece.setStreamId(stream.getStreamId());
		return level1ServerEngine.sendResponseHeaders(stream, headerPiece);
	}

	@Override
	public CompletableFuture<StreamWriter> sendPush(Http2Push push) {
		if(push.getStreamId() != 0 && push.getStreamId() != stream.getStreamId())
			throw new IllegalArgumentException("WE WILL SET the Http2Push.streamID so you should leave it as 0.   push="+push);
		else if(push.getPromisedStreamId() != 0)
			throw new IllegalArgumentException("WE WILL SET the Http2Push.promisedStreamId so you should leave it as 0.   push="+push);

		int promisedId = pushIdGenerator.getAndAdd(2);
		push.setPromisedStreamId(promisedId);
		return level1ServerEngine.sendPush(push);
	}

	@Override
	public void cancelStream() {
		RstStreamFrame frame = new RstStreamFrame();
		frame.setStreamId(stream.getStreamId());
		frame.setKnownErrorCode(Http2ErrorCode.CANCEL);
		level1ServerEngine.sendCancel(stream, frame);
	}

}
