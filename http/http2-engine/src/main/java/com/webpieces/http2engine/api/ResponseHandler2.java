package com.webpieces.http2engine.api;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2parser.api.dto.CancelReason;

public interface ResponseHandler2 {

	/**
	 * For Http2ClientEngine, this receives the Http2 Response and for Http2ServerEngine, you call this method
	 * to send a response
	 */
	CompletableFuture<StreamWriter> process(Http2Response response);

	/**
	 * For Http2ClientEngine, this receives the Http2 Push and for Http2ServerEngine, you call this method
	 * to send a push
	 */
	PushStreamHandle openPushStream();
	
	CompletableFuture<Void> cancel(CancelReason payload);


}