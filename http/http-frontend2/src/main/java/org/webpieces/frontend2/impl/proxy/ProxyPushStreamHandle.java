package org.webpieces.frontend2.impl.proxy;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2parser.api.dto.CancelReason;

public class ProxyPushStreamHandle implements PushStreamHandle {

	private PushStreamHandle pushStream;

	public ProxyPushStreamHandle(PushStreamHandle pushStream) {
		this.pushStream = pushStream;
	}

	@Override
	public CompletableFuture<PushPromiseListener> process(Http2Push headers) {
		//todo, wrap and add logs here too
		return pushStream.process(headers);
	}

	@Override
	public CompletableFuture<Void> cancelPush(CancelReason payload) {
		return pushStream.cancelPush(payload);
	}

}
