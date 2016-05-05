package org.webpieces.httpclient.impl;

import org.webpieces.httpclient.api.ResponseListener;

import com.webpieces.httpparser.api.dto.HttpRequest;

public class PendingRequest {

	private HttpRequest request;
	private ResponseListener listener;

	public PendingRequest(HttpRequest request, ResponseListener listener) {
		this.request = request;
		this.listener = listener;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public ResponseListener getListener() {
		return listener;
	}
	
}
