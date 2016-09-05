package org.webpieces.webserver.impl;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.BufferPool;
import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.httpparser.api.dto.HttpRequest;

public class RequestInfo {

	private RouterRequest routerRequest;
	private HttpRequest request;
	private BufferPool pool;
	private FrontendSocket channel;

	public RequestInfo(RouterRequest routerRequest, HttpRequest request, BufferPool pool, FrontendSocket channel) {
		this.routerRequest = routerRequest;
		this.request = request;
		this.pool = pool;
		this.channel = channel;
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

	public FrontendSocket getChannel() {
		return channel;
	}

}
