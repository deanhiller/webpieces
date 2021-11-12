package com.webpieces.http2.api.streaming;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;

public interface ResponseStreamHandle {

	/**
	 * For Http2ClientEngine, this receives the Http2 Response and for Http2ServerEngine, you call this method
	 * to send a response
	 */
	XFuture<StreamWriter> process(Http2Response response);

	/**
	 * This does nothing in http1.1. 
	 * 
	 * For Http2ClientEngine, this receives the Http2 Push and for Http2ServerEngine, you call this method
	 * to send a push
	 */
	PushStreamHandle openPushStream();
	
	
	/**
	 * If a request comes in, you can cancel right away if you are under load OR you can hold a reference to this
	 * handle to cancel later if any issue comes up in servicing the request
	 * 
	 * The method process above MUST NOT RETURN StreamRef which has cancel method as then it is too late and there
	 * are tests that will fail if this cancel is moved there....sooo, this must stay PLUS you would break clients which
	 * is even a bigger reason this can never be moved.
	 */
	XFuture<Void> cancel(CancelReason reason);

}