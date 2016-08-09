package org.webpieces.router.impl.ctx;

import org.webpieces.router.api.dto.RouterRequest;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.RouteMeta;

public class RequestContext {

	private ReverseRoutes reverseRoutes;
	private RouterRequest request;
	private RouteMeta routeMeta;

	public RequestContext(ReverseRoutes reverseRoutes, RouterRequest request, RouteMeta routeMeta) {
		this.reverseRoutes = reverseRoutes;
		this.request = request;
		this.routeMeta = routeMeta;
	}

	public ReverseRoutes getReverseRoutes() {
		return reverseRoutes;
	}

	public RouterRequest getRequest() {
		return request;
	}

	public RouteMeta getRouteMeta() {
		return routeMeta;
	}
	
}
