package org.webpieces.httpfrontend2.api.http2.mock;

import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.api.Protocol;

import com.webpieces.hpack.api.dto.Http2Headers;

public class RequestData {

	private FrontendStream stream;
	private Http2Headers headers;
	private Protocol type;

	public RequestData(FrontendStream stream, Http2Headers headers, Protocol type) {
		this.stream = stream;
		this.headers = headers;
		this.type = type;
	}

	public FrontendStream getStream() {
		return stream;
	}

	public Http2Headers getHeaders() {
		return headers;
	}

	public Protocol getType() {
		return type;
	}

}
