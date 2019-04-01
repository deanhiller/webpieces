package org.webpieces.router.impl.loader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.impl.FilterInfo;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.dto.MethodMeta;
import org.webpieces.router.impl.hooks.ControllerInfo;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;
import org.webpieces.router.impl.hooks.ServiceCreationInfo;
import org.webpieces.util.filters.Service;

@Singleton
public class ControllerLoader {

	private MetaLoaderProxy loader;
	private ControllerResolver resolver;

	private Set<BodyContentBinder> bodyBinderPlugins;
	private List<String> allBinderAnnotations = new ArrayList<>();
	
	@Inject
	public ControllerLoader(MetaLoaderProxy loader, ControllerResolver resolver) {
		this.loader = loader;
		this.resolver = resolver;
	}
	
	/**
	 * isInitializingAllControllers is true if in process of initializing ALL controllers and false if just being called to
	 * initialize on controller
	 * 
	 * @param meta
	 * @param isInitializingAllControllers
	 */
	public void loadControllerIntoMetaHtml(RouteMeta meta, boolean isInitializingAllControllers) {
		ResolvedMethod method = resolver.resolveControllerClassAndMethod(meta);
		
		//This is a hook for the dev server with auto-compile (if isInitializing, dev server skips this piece)
		//if not initializing, dev server does this piece.  Production does the opposite.

		ControllerInfo info = new ControllerInfo(meta.getInjector());
		LoadedController loadedController = loader.loadControllerIntoMeta(info, method, isInitializingAllControllers);
		if(loadedController == null)
			return;
		
		preconditionCheck(info, loadedController.getControllerMethod(), meta.getRoute().isPostOnly());
		
		meta.setControllerInstance(loadedController.getControllerInstance());
		meta.setMethod(loadedController.getControllerMethod());
		meta.setMethodParamNames(loadedController.getParamNames());
	}

	public void loadControllerIntoMetaContent(RouteMeta meta, boolean isInitializingAllControllers) {
		ResolvedMethod method = resolver.resolveControllerClassAndMethod(meta);
		
		//This is a hook for the dev server with auto-compile (if isInitializing, dev server skips this piece)
		//if not initializing, dev server does this piece.  Production does the opposite.
		ControllerInfo info = new ControllerInfo(meta.getInjector());
		LoadedController loadedController = loader.loadControllerIntoMeta(info, method, isInitializingAllControllers);
		if(loadedController == null)
			return;
		
		BodyContentBinder binder = contentPreconditionCheck(info, loadedController.getControllerMethod(), loadedController.getParameters());
		
		meta.setControllerInstance(loadedController.getControllerInstance());
		meta.setMethod(loadedController.getControllerMethod());
		meta.setMethodParamNames(loadedController.getParamNames());
		meta.setContentBinder(binder);
	}
	
	public void loadFiltersIntoMeta(RouteMeta m, boolean isInitializingAllFilters) {
		ServiceCreationInfo info = new ServiceCreationInfo(m.getInjector(), m.getFilters());
		Service<MethodMeta, Action> resp = loader.createServiceFromFilters(info, isInitializingAllFilters);
		m.setService(resp);
	}

	public Service<MethodMeta, Action> createNotFoundService(RouteMeta m, List<FilterInfo<?>> filterInfos) {
		ServiceCreationInfo info = new ServiceCreationInfo(m.getInjector(), filterInfos);
		return loader.createServiceFromFilters(info, false);
	}
	
	private void preconditionCheck(ControllerInfo meta, Method controllerMethod, boolean isPostOnly) {
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
	
	private BodyContentBinder contentPreconditionCheck(ControllerInfo meta, Method controllerMethod, Parameter[] parameters) {
		List<String> binderMatches = new ArrayList<>();
		AtomicReference<BodyContentBinder> lastMatch = new AtomicReference<BodyContentBinder>(null);
		for(BodyContentBinder binder : bodyBinderPlugins) {
			for(Parameter p : parameters) {
				Annotation[] annotations = p.getAnnotations();
				Class<?> entityClass = p.getType();
				recordParameterMatches(lastMatch, binderMatches, binder, annotations, entityClass);
			}
			
			Annotation[] annotations = controllerMethod.getAnnotations();
			recordParameterMatches(lastMatch, binderMatches, binder, annotations, null);
		}

		Class<?> returnType = controllerMethod.getReturnType();
		if(Action.class.isAssignableFrom(returnType))
			throw new IllegalArgumentException("The method for content route="+meta+" is returning Action and this is not allowed.  method="+controllerMethod);
		if(binderMatches.size() == 0)
			throw new IllegalArgumentException("there was not a single method parameter annotated with a Plugin"
					+ " annotation on method="+controllerMethod+".  looking at your\n"
					+ "plugins, these are the annotations available="+allBinderAnnotations+"\n"
					+ "You either need one parameter with one of the annotations OR\n"
					+ "you need to annotata the method(if it is read only and no request is supplied)");
		else if(binderMatches.size() > 1)
			throw new IllegalArgumentException("there is more than one parameter with a Plugin"
					+ " annotation on method="+controllerMethod+".  These\n"
					+ "are the ones we found(please delete one)="+binderMatches+"\n"		
					+ "Also make sure one parameter OR the method has the annotation, not both");

		
		
		return lastMatch.get();
	}
	
	private void recordParameterMatches(AtomicReference<BodyContentBinder> lastMatch, List<String> binderMatches, BodyContentBinder binder, Annotation[] annotations, Class<?> entityClass) {
		for(Annotation a : annotations) {
			if(binder.isManaged(entityClass, a.annotationType())) {
				binderMatches.add("@"+binder.getAnnotation().getSimpleName());
				lastMatch.set(binder);
			}
		}
	}
	
	public void install(Set<BodyContentBinder> bodyBinders) {
		this.bodyBinderPlugins = bodyBinders;
		for(BodyContentBinder binder : bodyBinders) {
			allBinderAnnotations.add("@"+binder.getAnnotation().getSimpleName());
		}
	}
}
