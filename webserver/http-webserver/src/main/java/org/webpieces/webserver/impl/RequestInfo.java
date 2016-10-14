package org.webpieces.webserver.impl;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.BufferPool;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpparser.api.dto.HttpRequest;

public class RequestInfo {

	private RouterRequest routerRequest;
	private HttpRequest request;
	private BufferPool pool;
	private ResponseSender responseSender;
	private ResponseId responseId;

	public RequestInfo(RouterRequest routerRequest, HttpRequest request, BufferPool pool, ResponseSender responseSender) {
		this.routerRequest = routerRequest;
		this.request = request;
		this.pool = pool;
		this.responseSender = responseSender;
	}

	public RouterRequest getRouterRequest() {
		return routerRequest;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public BufferPool getPool() {
		return pool;
	}

	public ResponseSender getResponseSender() {
		return responseSender;
	}

	public ResponseId getResponseId() {
		return responseId;
	}

	public void setResponseId(ResponseId responseId) {
		this.responseId = responseId;
	}
}
