package org.webpieces.webserver.dev.app;

import java.util.concurrent.CompletableFuture;

import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.router.impl.dto.MethodMeta;
import org.webpieces.util.filters.Service;

@Singleton
public class NotFoundFilter extends RouteFilter<Void> {

	@Override
	public CompletableFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
		if(meta.getCtx().getRequest().relativePath.startsWith("/enableFilter")) {
			return CompletableFuture.completedFuture(Actions.redirect(DevRouteId.FILTER_ROUTE));
		}
		return nextFilter.invoke(meta);
	}

	@Override
	public void initialize(Void initialConfig) {
	}

}
