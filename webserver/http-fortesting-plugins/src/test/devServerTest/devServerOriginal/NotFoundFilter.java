package org.webpieces.webserver.dev.app;

import java.util.concurrent.CompletableFuture;

import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routing.RouteFilter;
import org.webpieces.util.filters.Service;

@Singleton
public class NotFoundFilter extends RouteFilter<Void> {

	@Override
	public CompletableFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
		if(meta.getCtx().getRequest().relativePath.startsWith("/enableFilter")) {
			return CompletableFuture.completedFuture(Actions.redirect(DevRouteId.HOME));
		}
		return nextFilter.invoke(meta);
	}

	@Override
	public void initialize(Void initialConfig) {
	}

}
