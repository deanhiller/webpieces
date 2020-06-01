package com.webpieces.http2engine.api;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.dto.CancelReason;

/**
 * Ideally, I wanted to return CompletableFuture<StreamWriter> because most of the time if the response is blocked, we
 * don't want the body coming in and that puts backpressure on the clients.  However, if I did this, I have no way
 * of delivering a cancel to your StreamRef.  Therefore, this is a little more complex than I wanted as an api BUT
 * it makes up for that in the fact, it has all the power to do everything over http2
 */
public interface StreamRef {

	public CompletableFuture<StreamWriter> getWriter();

	public abstract CompletableFuture<Void> cancel(CancelReason reason);	
	
}
