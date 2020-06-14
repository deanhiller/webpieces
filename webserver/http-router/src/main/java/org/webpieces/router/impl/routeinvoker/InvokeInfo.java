package org.webpieces.router.impl.routeinvoker;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

public class InvokeInfo {

	private RequestContext requestCtx;
	private ProxyStreamHandle handler;
	private RouteType routeType;
	private LoadedController loadedController;
	private String i18nBundleName;

	public InvokeInfo(RequestContext requestCtx, ProxyStreamHandle handler, RouteType routeType, LoadedController loadedController, String i18nBundleName) {
		this.requestCtx = requestCtx;
		this.handler = handler;
		this.routeType = routeType;
		this.loadedController = loadedController;
		this.i18nBundleName = i18nBundleName;
	}

	public RequestContext getRequestCtx() {
		return requestCtx;
	}

	public ProxyStreamHandle getHandler() {
		return handler;
	}

	public RouteType getRouteType() {
		return routeType;
	}

	public String getI18nBundleName() {
		return i18nBundleName;
	}

	public LoadedController getLoadedController() {
		return loadedController;
	}
	
}
