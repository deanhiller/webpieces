package org.webpieces.webserver.dev.app;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.filters.Service;

import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

@Singleton
public class MyFilter extends RouteFilter<Void> {

	@Override
	public CompletableFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
		return CompletableFuture.completedFuture(Actions.redirect(DevRouteId.HOME));
	}

	@Override
	public void initialize(Void initialConfig) {
	}

}
