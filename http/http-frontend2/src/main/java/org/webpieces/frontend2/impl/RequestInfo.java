package org.webpieces.frontend2.impl;

import org.webpieces.util.futures.XFuture;

import org.webpieces.httpparser.api.dto.HttpRequest;

public class RequestInfo {

	private HttpRequest request;
	private XFuture<Void> future;

	public RequestInfo(HttpRequest request, XFuture<Void> future) {
		this.request = request;
		this.future = future;
	}
}
