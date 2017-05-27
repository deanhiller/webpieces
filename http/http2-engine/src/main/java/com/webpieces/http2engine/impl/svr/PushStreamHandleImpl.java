package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2parser.api.dto.CancelReason;

public class PushStreamHandleImpl implements PushStreamHandle {

	private AtomicInteger pushIdGenerator;
	private Level1ServerEngine level1ServerEngine;
	private ServerStream requestStream;

	public PushStreamHandleImpl(ServerStream stream, AtomicInteger pushIdGenerator, Level1ServerEngine level1ServerEngine) {
		this.requestStream = stream;
		this.pushIdGenerator = pushIdGenerator;
		this.level1ServerEngine = level1ServerEngine;
	}

	@Override
	public CompletableFuture<PushPromiseListener> process(Http2Push push) {
		if(push.getStreamId() != 0 && push.getStreamId() != requestStream.getStreamId())
			throw new IllegalArgumentException("WE WILL SET the Http2Push.streamID so you should leave it as 0.   push="+push);
		else if(push.getPromisedStreamId() != 0)
			throw new IllegalArgumentException("WE WILL SET the Http2Push.promisedStreamId so you should leave it as 0.   push="+push);

		int promisedId = pushIdGenerator.getAndAdd(2);
		push.setPromisedStreamId(promisedId);
		
		return level1ServerEngine.sendPush(this, push);
	}

	@Override
	public CompletableFuture<Void> cancelPush(CancelReason reset) {
		return level1ServerEngine.cancelPush(reset);
	}

}
