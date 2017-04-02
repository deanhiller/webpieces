package org.webpieces.webserver.impl;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.BufferPool;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpparser.api.dto.HttpRequest;

class RequestInfo {

	private RouterRequest routerRequest;
	private HttpRequest request;
	private BufferPool pool;
	private ResponseOverrideSender responseSender;
	private ResponseId responseId;
	private RequestId requestId;

	RequestInfo(RouterRequest routerRequest, HttpRequest request, RequestId requestId, BufferPool pool, ResponseOverrideSender responseSender) {
		this.routerRequest = routerRequest;
		this.request = request;
		this.pool = pool;
		this.responseSender = responseSender;
		this.requestId = requestId;
	}

	RouterRequest getRouterRequest() {
		return routerRequest;
	}

	public HttpRequest getRequest() {
		return request;
	}

	BufferPool getPool() {
		return pool;
	}

	ResponseOverrideSender getResponseSender() {
		return responseSender;
	}

	ResponseId getResponseId() {
		return responseId;
	}

	void setResponseId(ResponseId responseId) {
		this.responseId = responseId;
	}

	public RequestId getRequestId() {
		return requestId;
	}
}
