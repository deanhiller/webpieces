package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.ResponseHandler2;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.RstStreamFrame;

public class ResponseHandlerImpl implements ResponseHandler2 {

	private Level1ServerEngine level1ServerEngine;
	private ServerStream stream;
	private AtomicInteger pushIdGenerator;

	public ResponseHandlerImpl(Level1ServerEngine level1ServerEngine, ServerStream stream, AtomicInteger idGenerator) {
		this.level1ServerEngine = level1ServerEngine;
		this.stream = stream;
		this.pushIdGenerator = idGenerator;
	}

	@Override
	public CompletableFuture<StreamWriter> process(Http2Response response) {
		if(response.getStreamId() != stream.getStreamId())
			throw new IllegalArgumentException("The stream id is incorrect for this stream.  stream="+stream.getStreamId()+" frame(with bad streamId)="+response);
		else if(!response.isStatusSet())
			throw new IllegalArgumentException("The response must have the status header set.  bad frame="+response);
		
		response.setStreamId(stream.getStreamId());
		return level1ServerEngine.sendResponseHeaders(stream, response);
	}

	@Override
	public PushStreamHandle openPushStream() {
		return new PushStreamHandleImpl(stream, pushIdGenerator, level1ServerEngine);
	}

	@Override
	public CompletableFuture<Void> cancel(RstStreamFrame frame) {
		return level1ServerEngine.sendCancel(stream, frame);
	}

}
