package org.webpieces.router.api.routing;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.util.filters.Filter;

public abstract class RouteFilter<T> extends Filter<MethodMeta, Action> {

	public abstract void initialize(T initialConfig);

}
