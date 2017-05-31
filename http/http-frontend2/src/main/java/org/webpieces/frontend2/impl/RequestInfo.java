package org.webpieces.frontend2.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpparser.api.dto.HttpRequest;

public class RequestInfo {

	private HttpRequest request;
	private CompletableFuture<Void> future;

	public RequestInfo(HttpRequest request, CompletableFuture<Void> future) {
		this.request = request;
		this.future = future;
	}
}
