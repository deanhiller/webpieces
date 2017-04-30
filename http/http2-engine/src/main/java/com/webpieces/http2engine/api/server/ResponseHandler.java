package com.webpieces.http2engine.api.server;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;

public interface ResponseHandler {

	ServerStreamWriter sendResponse(Http2Headers headerPiece);
	
	ServerStreamWriter sendPush(Http2Push push);

	void cancelStream();
	
}
