package org.webpieces.router.impl.model;

import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.impl.Route;
import org.webpieces.router.impl.RouteImpl;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public abstract class AbstractDomainBuilder extends AbstractRouteBuilder{

	protected L2DomainRoutes domainRoutes;

	public AbstractDomainBuilder(RouterInfo routerInfo, L2DomainRoutes domainRoutes, L3PrefixedRouting routes, LogicHolder holder) {
		super(routerInfo, routes, holder);
		this.domainRoutes = domainRoutes;
	}

	private static final Logger log = LoggerFactory.getLogger(AbstractDomainBuilder.class);

	@Override
	public void setPageNotFoundRoute(String controllerMethod) {
		Route route = new RouteImpl(controllerMethod, RouteType.NOT_FOUND);
		setNotFoundRoute(route);
	}

	private void setNotFoundRoute(Route r) {
		if(!"".equals(this.routerInfo.getPath()))
			throw new UnsupportedOperationException("setNotFoundRoute can only be called on the root Router, not a scoped router");
		log.info("scope:'"+routerInfo+"' adding PAGE_NOT_FOUND route="+r.getFullPath()+" method="+r.getControllerMethodString());
		RouteMeta meta = new RouteMeta(r, injector.get(), currentPackage.get(), holder.getUrlEncoding());
		holder.getFinder().loadControllerIntoMetaObject(meta, true);
		domainRoutes.setPageNotFoundRoute(meta);
	}

	@Override
	public void setInternalErrorRoute(String controllerMethod) {
		Route route = new RouteImpl(controllerMethod, RouteType.INTERNAL_SERVER_ERROR);
		setInternalSvrErrorRoute(route);
	}
	
	private void setInternalSvrErrorRoute(Route r) {
		if(!"".equals(this.routerInfo.getPath()))
			throw new UnsupportedOperationException("setInternalSvrErrorRoute can only be called on the root Router, not a scoped router");
		log.info("scope:'"+routerInfo+"' adding INTERNAL_SVR_ERROR route="+r.getFullPath()+" method="+r.getControllerMethodString());
		RouteMeta meta = new RouteMeta(r, injector.get(), currentPackage.get(), holder.getUrlEncoding());
		holder.getFinder().loadControllerIntoMetaObject(meta, true);
		domainRoutes.setInternalSvrErrorRoute(meta);
	}
	
}
