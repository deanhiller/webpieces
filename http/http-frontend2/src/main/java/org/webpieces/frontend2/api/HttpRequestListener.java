package org.webpieces.frontend2.api;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.server.ServerStreamWriter;

public interface HttpRequestListener {

	ServerStreamWriter incomingRequest(FrontendStream stream, Http2Headers headers, Protocol type);

}
