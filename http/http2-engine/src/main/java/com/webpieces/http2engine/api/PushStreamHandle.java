package com.webpieces.http2engine.api;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2parser.api.dto.CancelReason;

public interface PushStreamHandle {

	CompletableFuture<PushPromiseListener> process(Http2Push headers);
	
	CompletableFuture<Void> cancelPush(CancelReason payload); 
}
