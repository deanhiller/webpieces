package com.webpieces.http2engine.api.server;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;

public interface ResponseHandler {

	CompletableFuture<StreamWriter> sendResponse(Http2Headers headerPiece);
	
	CompletableFuture<StreamWriter> sendPush(Http2Push push);

	void cancelStream();
	
}
