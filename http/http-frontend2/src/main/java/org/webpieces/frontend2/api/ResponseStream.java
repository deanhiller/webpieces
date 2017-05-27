package org.webpieces.frontend2.api;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;

public interface ResponseStream {

	CompletableFuture<StreamWriter> sendResponse(Http2Response headers);
	
	PushStreamHandle openPushStream();
	
	/**
	 * Cancel the request stream and all push streams as well.  
	 */
	CompletableFuture<Void> cancelStream();

	FrontendSocket getSocket();
	
    StreamSession getSession();

}
