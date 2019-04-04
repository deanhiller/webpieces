package org.webpieces.router.impl.loader;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.impl.BaseRouteInfo;
import org.webpieces.router.impl.FilterInfo;
import org.webpieces.router.impl.hooks.ControllerInfo;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;
import org.webpieces.router.impl.hooks.ServiceCreationInfo;
import org.webpieces.router.impl.loader.svc.MethodMeta;
import org.webpieces.router.impl.model.BodyContentBinderChecker;
import org.webpieces.router.impl.routebldr.RouteInfo;
import org.webpieces.router.impl.routers.DynamicInfo;
import org.webpieces.util.filters.Service;

import com.google.inject.Injector;

@Singleton
public class ControllerLoader {

	private final MetaLoaderProxy loader;
	private final ControllerResolver resolver;
	private BodyContentBinderChecker binderChecker;
	
	@Inject
	public ControllerLoader(
			MetaLoaderProxy loader, 
			ControllerResolver resolver,
			BodyContentBinderChecker binderChecker
	) {
		this.loader = loader;
		this.resolver = resolver;
		this.binderChecker = binderChecker;
	}

	public LoadedController loadHtmlController(Injector injector, RouteInfo routeInfo, boolean isInitializingAllControllers, boolean isPostOnly) {
		LoadedController loadedController = loadGenericController(injector, routeInfo, isInitializingAllControllers);
		if(loadedController != null)
			preconditionCheck(routeInfo, loadedController.getControllerMethod(), isPostOnly);
		return loadedController;
	}
	
	/**
	 * isInitializingAllControllers is true if in process of initializing ALL controllers and false if just being called to
	 * initialize on controller(which is done only in the DevServer)
	 * 
	 */
	public LoadedController loadGenericController(Injector injector, RouteInfo base, boolean isInitializingAllControllers) {
		//NOTE: We have to resolve both in production AND in DevServer right away so that we can give the stack trace
		//with customer code of the offending line if this first piece fails
		ToResolveInfo resolveInfo = new ToResolveInfo(injector, base.getControllerMethodString(), base.getRouteModuleInfo().packageName);
		ResolvedMethod method = resolver.resolveControllerClassAndMethod(resolveInfo);
		
		//This next piece is a hook for the dev server with auto-compile (if isInitializing, dev server skips this piece)
		//if not initializing, dev server does this piece.  Production does the opposite.
		ControllerInfo info = new ControllerInfo(resolveInfo.getInjector());
		return loader.loadControllerIntoMeta(info, method, isInitializingAllControllers);
	}
	
	public Service<MethodMeta, Action> loadFilters(BaseRouteInfo neededData, boolean isInitializingAllFilters) {
		return loadFilters(neededData, neededData.getFilters(), isInitializingAllFilters);
	}

	public Service<MethodMeta, Action> loadFilters(BaseRouteInfo neededData, List<FilterInfo<?>> filterInfos, boolean isInitializingAllFilters) {
		ServiceCreationInfo info = new ServiceCreationInfo(neededData.getInjector(), neededData.getService(), filterInfos);
		return loader.createServiceFromFilters(info, isInitializingAllFilters);
	}

	public DynamicInfo loadGenericControllerAndService(BaseRouteInfo route, boolean isInitializingAllControllers) {
		LoadedController controllerInst = loadGenericController(route.getInjector(), route.getRouteInfo(), isInitializingAllControllers);
		Service<MethodMeta, Action> service = loadFilters(route, isInitializingAllControllers);
		return new DynamicInfo(controllerInst, service);
	}

	public DynamicInfo loadControllerAndService(BaseRouteInfo route, boolean isInitializingAllControllers, boolean isPostOnly) {
		LoadedController controllerInst = loadHtmlController(route.getInjector(), route.getRouteInfo(), isInitializingAllControllers, isPostOnly);
		Service<MethodMeta, Action> service = loadFilters(route, isInitializingAllControllers);
		return new DynamicInfo(controllerInst, service);
	}
	
	private void preconditionCheck(Object meta, Method controllerMethod, boolean isPostOnly) {
		if(isPostOnly) {
			Class<?> clazz = controllerMethod.getReturnType();
			if(CompletableFuture.class.isAssignableFrom(clazz)) {
				Type genericReturnType = controllerMethod.getGenericReturnType();
				ParameterizedType t = (ParameterizedType) genericReturnType;
				Type type2 = t.getActualTypeArguments()[0];
				if(!(type2 instanceof Class))
					throw new IllegalArgumentException("Since this route="+meta+" is for POST, the method MUST return a type 'Redirect' or 'CompletableFuture<Redirect>' for this method="+controllerMethod);
				@SuppressWarnings("rawtypes")
				Class<?> type = (Class) type2;
				if(!Redirect.class.isAssignableFrom(type))
					throw new IllegalArgumentException("Since this route="+meta+" is for POST, the method MUST return a type 'Redirect' or 'CompletableFuture<Redirect>' not 'CompletableFuture<"+type.getSimpleName()+">'for this method="+controllerMethod);
			} else if(!Redirect.class.isAssignableFrom(clazz))
				throw new IllegalArgumentException("Since this route="+meta+" is for POST, the method MUST return a type 'Redirect' or 'CompletableFuture<Redirect>' not '"+clazz.getSimpleName()+"' for this method="+controllerMethod);
		} else {
			Class<?> clazz = controllerMethod.getReturnType();
			if(CompletableFuture.class.isAssignableFrom(clazz)) {
				Type genericReturnType = controllerMethod.getGenericReturnType();
				ParameterizedType t = (ParameterizedType) genericReturnType;
				Type type2 = t.getActualTypeArguments()[0];
				if(!(type2 instanceof Class))
					throw new IllegalArgumentException("This route="+meta+" has a method that MUST return a type 'Action' or 'CompletableFuture<Action>' for this method(and did not)="+controllerMethod);
				@SuppressWarnings("rawtypes")
				Class<?> type = (Class) type2;
				if(!Action.class.isAssignableFrom(type))
					throw new IllegalArgumentException("This route="+meta+" has a method that MUST return a type 'Action' or 'CompletableFuture<Action>' not 'CompletableFuture<"+type.getSimpleName()+">'for this method="+controllerMethod);
			} else if(!Action.class.isAssignableFrom(clazz))
				throw new IllegalArgumentException("This route="+meta+" has a method that MUST return a type 'Action' or 'CompletableFuture<Action>' not '"+clazz.getSimpleName()+"' for this method="+controllerMethod);
		}
	}

	public BinderAndLoader loadContentController(Injector injector, RouteInfo routeInfo, boolean isInitializingAllControllers) {
		LoadedController loadedController = loadGenericController(injector, routeInfo, isInitializingAllControllers);
		
		BodyContentBinder binder = null;
		if(loadedController != null)
			binder = binderChecker.contentPreconditionCheck(injector, routeInfo, loadedController.getControllerMethod(), loadedController.getParameters());

		return new BinderAndLoader(loadedController, binder);
	}
}
