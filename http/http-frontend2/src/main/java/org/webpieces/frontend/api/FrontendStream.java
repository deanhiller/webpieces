package org.webpieces.frontend.api;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;

public interface FrontendStream {

	StreamWriter sendResponse(Http2Headers headers);
	
	StreamWriter sendPush(Http2Push push);
	
	void cancelStream();

	FrontendSocket getSocket();
}
