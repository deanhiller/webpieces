package org.webpieces.router.impl.model;

import org.webpieces.router.api.routing.PortType;
import org.webpieces.router.api.routing.RouteFilter;
import org.webpieces.router.api.routing.Router;

public class R2DomainRouterBuilder extends AbstractDomainBuilder {

	public R2DomainRouterBuilder(RouterInfo info, L2DomainRoutes domainRoutes, L3PrefixedRouting routes, LogicHolder holder) {
		super(info, domainRoutes, routes, holder);
	}
	
	@Override
	public Router getDomainScopedRouter(String domainRegEx) {
		throw new UnsupportedOperationException("Cannot getDomainScopedRouter on a DomainScopedRouter");
	}

	@Override
	public <T> void addFilter(String path, Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type) {
		throw new UnsupportedOperationException("must be called on non-scoped router");
	}

	@Override
	public <T> void addNotFoundFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type) {
		throw new UnsupportedOperationException("must be called on non-scoped router");
	}

	@Override
	public <T> void addInternalErrorFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type) {
		throw new UnsupportedOperationException("must be called on non-scoped router");
	}

	@Override
	public void addStaticDir(String urlPath, String fileSystemPath, boolean isOnClassPath) {
		throw new UnsupportedOperationException("must be called on non-scoped router");
	}

	@Override
	public void addStaticFile(String urlPath, String fileSystemPath, boolean isOnClassPath) {
		throw new UnsupportedOperationException("must be called on non-scoped router");
	}

}
