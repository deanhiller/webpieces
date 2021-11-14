package org.webpieces.webserver.dev.app;

import org.webpieces.util.futures.XFuture;

import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.util.filters.Service;

@Singleton
public class NotFoundFilter extends RouteFilter<Void> {

	@Override
	public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
		if(meta.getCtx().getRequest().relativePath.startsWith("/enableFilter")) {
			return XFuture.completedFuture(Actions.redirect(DevRouteId.FILTER_ROUTE));
		}
		return nextFilter.invoke(meta);
	}

	@Override
	public void initialize(Void initialConfig) {
	}

}
