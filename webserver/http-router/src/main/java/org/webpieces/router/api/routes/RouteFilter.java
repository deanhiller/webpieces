package org.webpieces.router.api.routes;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.util.filters.Filter;
import org.webpieces.util.futures.FutureHelper;

public abstract class RouteFilter<T> extends Filter<MethodMeta, Action> {

	public abstract void initialize(T initialConfig);

}
