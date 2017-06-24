package org.webpieces.webserver.test;

import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpRequest;

public class Http11FullRequest {

	private HttpRequest request;
	private HttpData data;

	public Http11FullRequest(HttpRequest request, HttpData data) {
		this.request = request;
		this.data = data;
	}

	public void addHeader(Header header) {
		request.addHeader(header);
	}

	public HttpRequest getRequest() {
		return request;
	}

	public HttpData getData() {
		return data;
	}
	
}