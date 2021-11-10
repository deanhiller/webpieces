package com.webpieces.http2engine.impl.svr;

import org.webpieces.util.futures.XFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.PushPromiseListener;
import com.webpieces.http2.api.streaming.PushStreamHandle;

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
	public XFuture<PushPromiseListener> process(Http2Push push) {
		if(push.getStreamId() != 0 && push.getStreamId() != requestStream.getStreamId())
			throw new IllegalArgumentException("WE WILL SET the Http2Push.streamID so you should leave it as 0.   push="+push);
		else if(push.getPromisedStreamId() != 0)
			throw new IllegalArgumentException("WE WILL SET the Http2Push.promisedStreamId so you should leave it as 0.   push="+push);

		int promisedId = pushIdGenerator.getAndAdd(2);
		push.setPromisedStreamId(promisedId);
		
		return level1ServerEngine.sendPush(this, push);
	}

	@Override
	public XFuture<Void> cancelPush(CancelReason reset) {
		return level1ServerEngine.cancelPush(reset);
	}

}
