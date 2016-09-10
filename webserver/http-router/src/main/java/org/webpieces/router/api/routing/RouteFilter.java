package org.webpieces.router.api.routing;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.util.filters.Filter;

public interface RouteFilter<T> extends Filter<MethodMeta, Action> {

	void initialize(T initialConfig);

}
