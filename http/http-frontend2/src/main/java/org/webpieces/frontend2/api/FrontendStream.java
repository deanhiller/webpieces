package org.webpieces.frontend2.api;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;

public interface FrontendStream {

	CompletableFuture<StreamWriter> sendResponse(Http2Headers headers);
	
	CompletableFuture<StreamWriter> sendPush(Http2Push push);
	
	void cancelStream();

	FrontendSocket getSocket();
}
