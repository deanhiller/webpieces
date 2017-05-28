package org.webpieces.httpparser.api;

import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpRequest;

public class HttpDummyRequest {

	private HttpRequest request;
	private HttpData httpData;

	public HttpDummyRequest(HttpRequest request, HttpData httpData) {
		this.request = request;
		this.httpData = httpData;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public HttpData getHttpData() {
		return httpData;
	}

}
