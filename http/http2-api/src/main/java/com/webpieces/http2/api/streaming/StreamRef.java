package com.webpieces.http2.api.streaming;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;

/**
 * Ideally, I wanted to return XFuture<StreamWriter> because most of the time if the response is blocked, we
 * don't want the body coming in and that puts backpressure on the clients.  However, if I did this, I have no way
 * of delivering a cancel to your StreamRef.  Therefore, this is a little more complex than I wanted as an api BUT
 * it makes up for that in the fact, it has all the power to do everything over http2
 */
public interface StreamRef {

	/**
	 * for backpressure so you can return a future to backpressure a client AND can asynchronously connect to 
	 * a remote host.  ie. you can do something like this
	 * 
	 *  1. XFuture<Void> connectFuture = httpSocket.connect(host);
	 *  2. XFuture<Response> responseFut = connectFuture.thenCompose(voidd -> httpSocket.send(authenticationRequest));
	 *  3. XFuture<StreamWriter> writerFut = responseFut.thenCompose(resp -> createWriter(resp));
	 *  4. Return a StreamRef with XFuture<StreamWriter>
	 *  
	 *  This ensures we only start writing (or backpressure until your app code is ready to consume) once you give us the stream
	 *  writer by completing the future in step 3.
	 *  
	 * @return
	 */
	XFuture<StreamWriter> getWriter();

	XFuture<Void> cancel(CancelReason reason);
}
