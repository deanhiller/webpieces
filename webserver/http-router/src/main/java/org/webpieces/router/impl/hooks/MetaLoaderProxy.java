package org.webpieces.router.impl.hooks;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.loader.ProdLoader;
import org.webpieces.router.impl.loader.ResolvedMethod;
import org.webpieces.util.filters.Service;

import com.google.inject.ImplementedBy;
import com.google.inject.Injector;

@ImplementedBy(ProdLoader.class)
public interface MetaLoaderProxy {

	LoadedController loadControllerIntoMeta(Injector injector, ResolvedMethod method);

	Service<MethodMeta, Action> createServiceFromFilters(ServiceCreationInfo info);

}
