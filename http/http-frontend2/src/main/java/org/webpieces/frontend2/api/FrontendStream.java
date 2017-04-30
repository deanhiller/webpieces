package org.webpieces.frontend2.api;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.server.ServerStreamWriter;

public interface FrontendStream {

	ServerStreamWriter sendResponse(Http2Headers headers);
	
	ServerStreamWriter sendPush(Http2Push push);
	
	void cancelStream();

	FrontendSocket getSocket();
}
