package org.webpieces.router.impl.loader;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.controller.actions.Render;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;
import org.webpieces.router.impl.hooks.ServiceCreationInfo;
import org.webpieces.router.impl.model.BodyContentBinderChecker;
import org.webpieces.router.impl.routebldr.FilterCreationMeta;
import org.webpieces.router.impl.routebldr.RouteInfo;
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

	public LoadedController loadNotFoundController(Injector injector, RouteInfo route, boolean isInitializingAllControllers) {
		LoadedController loadedController = loadGenericController(injector, route, isInitializingAllControllers).getLoadedController();
		if(loadedController != null) {
			preconditionCheckForErrorRoute(loadedController);
		}
		return loadedController;
	}
	
	public LoadedController loadErrorController(Injector injector, RouteInfo route, boolean isInitializingAllControllers) {
		LoadedController loadedController = loadGenericController(injector, route, isInitializingAllControllers).getLoadedController();
		if(loadedController != null) {
			preconditionCheckForErrorRoute(loadedController);
		}
		return loadedController;
	}

	private void preconditionCheckForErrorRoute(LoadedController loadedController) {
		Method controllerMethod = loadedController.getControllerMethod();
		
		Class<?> clazz = controllerMethod.getReturnType();
		if(CompletableFuture.class.isAssignableFrom(clazz)) {
			Type genericReturnType = controllerMethod.getGenericReturnType();
			ParameterizedType t = (ParameterizedType) genericReturnType;
			Type type2 = t.getActualTypeArguments()[0];
			if(!(type2 instanceof Class))
				throw new IllegalArgumentException("This error route has a method that MUST return a type 'Render' or 'CompletableFuture<Render>' "
						+ "for this method(and did not)="+controllerMethod);
			@SuppressWarnings("rawtypes")
			Class<?> type = (Class) type2;
			if(!Render.class.isAssignableFrom(type))
				throw new IllegalArgumentException("This error route has a method that MUST return a type 'Render' or 'CompletableFuture<Render>' not 'CompletableFuture<"+type.getSimpleName()+">'for this method="+controllerMethod);
		} else if(!Render.class.isAssignableFrom(clazz))
			throw new IllegalArgumentException("This error route has a method that MUST return a type 'Render' or 'CompletableFuture<Render>' not '"+clazz.getSimpleName()+"' for this method="+controllerMethod);
	}
	
	public BinderAndLoader loadContentController(Injector injector, RouteInfo routeInfo, boolean isInitializingAllControllers) {
		MethodMetaAndController mAndC = loadGenericController(injector, routeInfo, isInitializingAllControllers);
		BodyContentBinder binder = null;
		LoadedController loadedController = mAndC.getLoadedController();
		if(loadedController != null)
			binder = binderChecker.contentPreconditionCheck(injector, routeInfo, loadedController.getControllerMethod(), loadedController.getParameters());

		return new BinderAndLoader(mAndC, binder);
	}
	
	public MethodMetaAndController loadHtmlController(Injector injector, RouteInfo routeInfo, boolean isInitializingAllControllers, boolean isPostOnly) {
		MethodMetaAndController mAndC = loadGenericController(injector, routeInfo, isInitializingAllControllers);
		if(mAndC.getLoadedController() != null)
			htmlPreconditionCheck(routeInfo, mAndC.getLoadedController().getControllerMethod(), isPostOnly);
		return mAndC;
	}
	
	/**
	 * isInitializingAllControllers is true if in process of initializing ALL controllers and false if just being called to
	 * initialize on controller(which is done only in the DevServer)
	 */
	public MethodMetaAndController loadGenericController(Injector injector, RouteInfo base, boolean isInitializingAllControllers) {
		ResolvedMethod method = resolver.resolveControllerClassAndMethod(base);
		LoadedController loadedController = loader.loadControllerIntoMeta(injector, method, isInitializingAllControllers);
		return new MethodMetaAndController(method, loadedController);
	}
	
	public Service<MethodMeta, Action> loadFilters(FilterCreationMeta neededData, boolean isInitializingAllFilters) {
		ServiceCreationInfo info = new ServiceCreationInfo(neededData.getInjector(), neededData.getService(), neededData.getFilters());
		return loader.createServiceFromFilters(info, isInitializingAllFilters);
	}

	private void htmlPreconditionCheck(Object meta, Method controllerMethod, boolean isPostOnly) {
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

}
