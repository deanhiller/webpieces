package com.webpieces.http2engine.api;

import com.webpieces.hpack.api.dto.Http2Request;

public interface RequestStreamHandle {

	StreamRef process(Http2Request request, ResponseStreamHandle responseListener);
	
}
