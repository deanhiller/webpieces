package org.webpieces.webserver.dev.app;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.api.routing.RouteFilter;
import org.webpieces.util.filters.Service;
import org.webpieces.webserver.basic.app.biz.SomeLib;

@Singleton
public class NotFoundFilter extends RouteFilter<Void> {

	@Override
	public CompletableFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
		return nextFilter.invoke(meta);
	}

	@Override
	public void initialize(Void initialConfig) {
	}

}
