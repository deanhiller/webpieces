package org.webpieces.router.impl.model;

import org.webpieces.router.api.routing.PortType;
import org.webpieces.router.api.routing.RouteFilter;
import org.webpieces.router.api.routing.Router;

public class R3PrefixRouterBuilder extends AbstractRouteBuilder {

	public R3PrefixRouterBuilder(RouterInfo info, L3PrefixedRouting routes, LogicHolder holder, boolean isHttpsOnlyRoutes) {
		super(info, routes, holder, isHttpsOnlyRoutes);
	}

	@Override
	public Router getDomainScopedRouter(String domainRegEx) {
		//would be nice to make our apis more type-safe(compile error instead of runtime) but that would require major 
		//changes to api...breaking changes
		throw new UnsupportedOperationException("You are misusing the api as a client.  this can only be done on base router");
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

	@Override
	public void setPageNotFoundRoute(String controllerMethod) {
		throw new UnsupportedOperationException("must be called on non-scoped router or domain scoped router");
	}

	@Override
	public void setInternalErrorRoute(String controllerMethod) {
		throw new UnsupportedOperationException("must be called on non-scoped router or domain scoped router");
	}

}
