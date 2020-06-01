package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.ResponseStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.RstStreamFrame;

public class SvrSideResponseHandler implements ResponseStreamHandle {

	private Level1ServerEngine level1ServerEngine;
	private ServerStream stream;
	private AtomicInteger pushIdGenerator;

	public SvrSideResponseHandler(Level1ServerEngine level1ServerEngine, ServerStream stream, AtomicInteger idGenerator) {
		this.level1ServerEngine = level1ServerEngine;
		this.stream = stream;
		this.pushIdGenerator = idGenerator;
	}

	@Override
	public CompletableFuture<StreamWriter> process(Http2Response response) {
		response.setStreamId(stream.getStreamId());

		if(!response.isStatusSet())
			throw new IllegalArgumentException("The response must have the status header set.  bad frame="+response);
		
		return level1ServerEngine.sendResponseHeaders(stream, response);
	}

	@Override
	public PushStreamHandle openPushStream() {
		return new PushStreamHandleImpl(stream, pushIdGenerator, level1ServerEngine);
	}

	@Override
	public CompletableFuture<Void> cancel(CancelReason frame) {
		if(!(frame instanceof RstStreamFrame))
			throw new IllegalArgumentException("App can only pass in RstStreamFrame object here to be sent to clients.  The api is for consistency and shared with client");
		return level1ServerEngine.sendCancel(stream, (RstStreamFrame)frame);
	}

}
