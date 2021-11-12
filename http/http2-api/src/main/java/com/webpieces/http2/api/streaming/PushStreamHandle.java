package com.webpieces.http2.api.streaming;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;

public interface PushStreamHandle {

	XFuture<PushPromiseListener> process(Http2Push headers);
	
	XFuture<Void> cancelPush(CancelReason payload); 
}
