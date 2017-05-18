package org.webpieces.webserver.impl;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.BufferPool;

import com.webpieces.hpack.api.dto.Http2Headers;

class RequestInfo {

	private RouterRequest routerRequest;
	private Http2Headers request;
	private BufferPool pool;
	private ResponseOverrideSender responseSender;

	RequestInfo(RouterRequest routerRequest, Http2Headers request, BufferPool pool, ResponseOverrideSender responseSender) {
		this.routerRequest = routerRequest;
		this.request = request;
		this.pool = pool;
		this.responseSender = responseSender;
	}

	RouterRequest getRouterRequest() {
		return routerRequest;
	}

	public Http2Headers getRequest() {
		return request;
	}

	BufferPool getPool() {
		return pool;
	}

	ResponseOverrideSender getResponseSender() {
		return responseSender;
	}

}
