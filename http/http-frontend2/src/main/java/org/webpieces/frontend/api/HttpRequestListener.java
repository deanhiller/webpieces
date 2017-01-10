package org.webpieces.frontend.api;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;

public interface HttpRequestListener {

	StreamWriter incomingRequest(FrontendStream stream, Http2Headers headers, RequestType type);

}
