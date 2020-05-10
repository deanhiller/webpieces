package org.webpieces.router.impl.proxyout.filereaders;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.BufferPool;

import com.webpieces.hpack.api.dto.Http2Request;
import org.webpieces.router.impl.proxyout.ResponseOverrideSender;

public class RequestInfo {

	private RouterRequest routerRequest;
	private Http2Request request;
	private BufferPool pool;
	private ResponseOverrideSender responseSender;

	public RequestInfo(RouterRequest routerRequest, Http2Request request, BufferPool pool, ResponseOverrideSender responseSender) {
		this.routerRequest = routerRequest;
		this.request = request;
		this.pool = pool;
		this.responseSender = responseSender;
	}

	public RouterRequest getRouterRequest() {
		return routerRequest;
	}

	public Http2Request getRequest() {
		return request;
	}

	public BufferPool getPool() {
		return pool;
	}

	public ResponseOverrideSender getResponseSender() {
		return responseSender;
	}

}