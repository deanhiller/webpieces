package org.webpieces.router.impl.routebldr;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.MethodMetaAndController;
import org.webpieces.router.impl.routers.AbstractDynamicRouter;
import org.webpieces.util.filters.Service;

public class RouterAndInfo {

	private final AbstractDynamicRouter router;
	private final RouteInfo routeInfo;
	private final RouteType routeType;
	private final MethodMetaAndController metaAndController;
	private final Service<MethodMeta, Action>  svcProxy;

	public RouterAndInfo(AbstractDynamicRouter router, RouteInfo routeInfo, RouteType routeType, MethodMetaAndController metaAndController, Service<MethodMeta, Action>  svc) {
		this.router = router;
		this.routeInfo = routeInfo;
		this.routeType = routeType;
		this.metaAndController = metaAndController;
		this.svcProxy = svc;
	}

	public AbstractDynamicRouter getRouter() {
		return router;
	}

	public RouteInfo getRouteInfo() {
		return routeInfo;
	}

	public RouteType getRouteType() {
		return routeType;
	}

	public MethodMetaAndController getMetaAndController() {
		return metaAndController;
	}

	public Service<MethodMeta, Action> getSvcProxy() {
		return svcProxy;
	}

}
