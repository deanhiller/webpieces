package org.webpieces.webserver.test;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Trailers;
import com.webpieces.http2parser.api.dto.DataFrame;

public class Http2FullRequest {

	private Http2Request request;
	private DataFrame data;
	private Http2Trailers trailers;
	
	public Http2FullRequest(Http2Request request, DataFrame data, Http2Trailers trailers) {
		super();
		this.request = request;
		this.data = data;
		this.trailers = trailers;
	}

	public Http2Request getRequest() {
		return request;
	}

	public DataFrame getData() {
		return data;
	}

	public Http2Trailers getTrailers() {
		return trailers;
	}

}
