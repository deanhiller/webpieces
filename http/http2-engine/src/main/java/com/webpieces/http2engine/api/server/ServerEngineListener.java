package com.webpieces.http2engine.api.server;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;

public interface ServerEngineListener {

	/**
	 * A request is either headers only, headers plus data, more data, more data(using StreamWriter), or
	 * headers, data, data, data, headers, OR headers, data, data, stream reset(ie. cancel)
	 */
	StreamWriter receiveRequest(Http2Headers request, ResponseHandler responseHandler);

}
