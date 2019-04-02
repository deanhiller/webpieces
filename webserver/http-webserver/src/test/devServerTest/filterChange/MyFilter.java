package org.webpieces.webserver.dev.app;

import java.util.concurrent.CompletableFuture;

import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.router.impl.loader.svc.MethodMeta;
import org.webpieces.util.filters.Service;

@Singleton
public class MyFilter extends RouteFilter<Void> {

	@Override
	public CompletableFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
		return CompletableFuture.completedFuture(Actions.redirect(DevRouteId.CAUSE_ERROR));
	}

	@Override
	public void initialize(Void initialConfig) {
	}

}
