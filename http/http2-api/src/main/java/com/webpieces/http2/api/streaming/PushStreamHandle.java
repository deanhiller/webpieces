package com.webpieces.http2.api.streaming;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;

public interface PushStreamHandle {

	CompletableFuture<PushPromiseListener> process(Http2Push headers);
	
	CompletableFuture<Void> cancelPush(CancelReason payload); 
}
