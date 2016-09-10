package org.webpieces.webserver.dev.app;

import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.api.routing.RouteFilter;
import org.webpieces.util.filters.Service;

public class NotFoundFilter implements RouteFilter<Void> {

	@Override
	public CompletableFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
		return CompletableFuture.completedFuture(Actions.redirect(DevRouteId.HOME));
	}

	@Override
	public void initialize(Void initialConfig) {
	}

}
