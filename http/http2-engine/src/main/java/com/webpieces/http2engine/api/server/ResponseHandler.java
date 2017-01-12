package com.webpieces.http2engine.api.server;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;

public interface ResponseHandler {

	StreamWriter sendResponse(Http2Headers headerPiece);
	
	StreamWriter sendPush(Http2Push push);

	void cancelStream();
	
}
