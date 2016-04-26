package org.webpieces.httpclient.api;

import com.webpieces.httpparser.api.dto.HttpResponse;

public class Response {

	private HttpSocket socket;
	private HttpResponse response;
	
	public Response(HttpSocket socket, HttpResponse response) {
		this.socket = socket;
		this.response = response;
	}
	
	public HttpSocket getSocket() {
		return socket;
	}
	public HttpResponse getResponse() {
		return response;
	}
}
