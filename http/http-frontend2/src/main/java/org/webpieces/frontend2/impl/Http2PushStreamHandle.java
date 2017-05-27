package org.webpieces.frontend2.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2parser.api.dto.CancelReason;

public class Http2PushStreamHandle implements PushStreamHandle {

	private PushStreamHandle pushStream;
	private AtomicBoolean isResponseSent;
	private boolean isPushSent;

	public Http2PushStreamHandle(PushStreamHandle pushStream, AtomicBoolean isResponseSent) {
		this.pushStream = pushStream;
		this.isResponseSent = isResponseSent;
	}

	@Override
	public CompletableFuture<PushPromiseListener> process(Http2Push headers) {
		if(isResponseSent.get())
			throw new IllegalStateException("You must call openPushStream AND send just the Http2Push "
					+ "before sendResponse, but after "
					+ "that can send both datastreams back at the "
					+ "same time(see http2 spec for why).  it also helps memory pressure this way");
		else if(isPushSent)
			throw new IllegalStateException("You can only send ONE Http2Push on each Push Stream");

		isPushSent = true;
		return pushStream.process(headers);
	}

	@Override
	public CompletableFuture<Void> cancelPush(CancelReason reset) {
		return pushStream.cancelPush(reset);
	}

}
