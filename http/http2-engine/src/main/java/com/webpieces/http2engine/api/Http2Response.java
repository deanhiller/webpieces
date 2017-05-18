package com.webpieces.http2engine.api;

import com.webpieces.hpack.api.dto.Http2Headers;

public class Http2Response {

	private Http2Headers headers;

	public Http2Response(Http2Headers headers) {
		this.headers = headers;
	}

	public Http2Headers getHeaders() {
		return headers;
	}

}
