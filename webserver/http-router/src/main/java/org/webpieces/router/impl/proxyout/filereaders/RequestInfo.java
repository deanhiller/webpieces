package org.webpieces.router.impl.proxyout.filereaders;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.BufferPool;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

import com.webpieces.hpack.api.dto.Http2Request;

public class RequestInfo {

	private RouterRequest routerRequest;
	private Http2Request request;
	private BufferPool pool;
	private ProxyStreamHandle responseSender;

	public RequestInfo(RouterRequest routerRequest, Http2Request request, BufferPool pool, ProxyStreamHandle responseSender) {
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

	public ProxyStreamHandle getResponseSender() {
		return responseSender;
	}

}
