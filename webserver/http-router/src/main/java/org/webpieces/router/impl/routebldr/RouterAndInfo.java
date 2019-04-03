package org.webpieces.router.impl.routebldr;

import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.routers.HtmlRouter;

public class RouterAndInfo {

	private final HtmlRouter router;
	private final RouteInfo routeInfo;
	private final RouteType routeType;
	private final LoadedController loadedController;

	public RouterAndInfo(HtmlRouter router, RouteInfo routeInfo, RouteType routeType, LoadedController loadedController) {
		this.router = router;
		this.routeInfo = routeInfo;
		this.routeType = routeType;
		this.loadedController = loadedController;
	}

	public HtmlRouter getRouter() {
		return router;
	}

	public RouteInfo getRouteInfo() {
		return routeInfo;
	}

	public RouteType getRouteType() {
		return routeType;
	}

	public LoadedController getLoadedController() {
		return loadedController;
	}

}
