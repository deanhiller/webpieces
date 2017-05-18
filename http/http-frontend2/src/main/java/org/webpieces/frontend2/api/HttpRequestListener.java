package org.webpieces.frontend2.api;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.RstStreamFrame;

public interface HttpRequestListener {

	StreamWriter incomingRequest(FrontendStream stream, Http2Headers headers, SocketInfo info);

	void cancelRequest(FrontendStream stream, RstStreamFrame c);

}
