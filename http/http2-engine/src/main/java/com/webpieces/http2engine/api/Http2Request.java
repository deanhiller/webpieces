package com.webpieces.http2engine.api;

import com.webpieces.hpack.api.dto.Http2Headers;

public class Http2Request {

	private Http2Headers headers;

	public Http2Request(Http2Headers headers) {
		this.headers = headers;
	}

	public Http2Headers getHeaders() {
		return headers;
	}

}
