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

	/**
	 * for backpressure so you can return a future to backpressure a client AND can asynchronously connect to 
	 * a remote host.  ie. you can do something like this
	 * 
	 *  1. CompletableFuture<Void> connectFuture = httpSocket.connect(host);
	 *  2. CompletableFuture<Response> responseFut = connectFuture.thenCompose(voidd -> httpSocket.send(authenticationRequest));
	 *  3. CompletableFuture<StreamWriter> writerFut = responseFut.thenCompose(resp -> createWriter(resp));
	 *  4. Return a StreamRef with CompletableFuture<StreamWriter>
	 *  
	 *  This ensures we only start writing (or backpressure until your app code is ready to consume) once you give us the stream
	 *  writer by completing the future in step 3.
	 *  
	 * @return
	 */
	CompletableFuture<StreamWriter> getWriter();

	CompletableFuture<Void> cancel(CancelReason reason);
}
