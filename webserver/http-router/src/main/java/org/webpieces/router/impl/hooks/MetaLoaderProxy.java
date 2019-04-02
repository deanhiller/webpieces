package org.webpieces.router.impl.hooks;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.loader.ResolvedMethod;
import org.webpieces.router.impl.loader.svc.MethodMeta;
import org.webpieces.util.filters.Service;

public interface MetaLoaderProxy {

	LoadedController loadControllerIntoMeta(ControllerInfo meta, ResolvedMethod method,
			boolean isInitializingAllControllers);

	Service<MethodMeta, Action> createServiceFromFilters(ServiceCreationInfo info, boolean isInitializingAllFilters);

}
