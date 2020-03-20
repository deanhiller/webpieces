package org.webpieces.webserver.filters.app;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.filters.Service;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class StatefulFilter extends RouteFilter<Integer> {

	private Integer initialConfig;
	private Remote svc;

	@Inject
	public StatefulFilter(Remote svc) {
		this.svc = svc;
	}
	
	@Override
	public void initialize(Integer initialConfig) {
		this.initialConfig = initialConfig;
	}

	@Override
	public CompletableFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
		svc.record(initialConfig);
		return nextFilter.invoke(meta);
	}

}
