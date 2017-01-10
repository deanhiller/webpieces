package com.webpieces.http2engine.api.server;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;

public interface Http2ServerEngine {
	/**
	 * Future completes one the data is SENT! not when there is a response
	 */
	StreamWriter incomingRequest(FrontendStream stream, Http2Headers req);

	/**
	 * completely tear down engine
	 */
	void farEndClosed();
}
