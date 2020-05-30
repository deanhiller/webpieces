package com.webpieces.http2engine.api;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2parser.api.dto.CancelReason;

public interface StreamHandle {

	CompletableFuture<StreamWriter> process(Http2Request request, ResponseHandler responseListener);
	
	/**
	 * Because the app may return a completed future from process, we must have a cancel that the
	 * platform invokes such that an app can clean up any state with that request stream.  On the other hand,
	 * if the app returned an unresolved future, we do not have the StreamWriter to call cancel on so cancel canNOT
	 * be put on the StreamWriter(though we could cancel the future if that was the case).  We could make the api
	 * confusing by cancelling the future and if it resulted in being cancelled properly, we could call this method
	 * but then the app developer would have to program up handling two different cancels(annoying).  Instead, this
	 * is the ONLY cancel that will be called
	 * 
	 * @param frame
	 * @return 
	 */
	CompletableFuture<Void> cancel(CancelReason payload); 
}
