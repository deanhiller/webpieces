package org.webpieces.router.impl.loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.BodyContentBinder;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.RenderContent;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.params.ArgsResult;
import org.webpieces.router.impl.params.ParamToObjectTranslatorImpl;
import org.webpieces.util.filters.Service;

public class ServiceProxy implements Service<MethodMeta, Action> {

	private ParamToObjectTranslatorImpl translator;

	public ServiceProxy(ParamToObjectTranslatorImpl translator) {
		this.translator = translator;
	}
	
	@Override
	public CompletableFuture<Action> invoke(MethodMeta meta) {
		try {
			return invokeMethod(meta);
		} catch(InvocationTargetException e) {
			Throwable cause = e.getCause();
			if(cause instanceof NotFoundException) {
				return createNotFound((NotFoundException) cause);
			}
			return createRuntimeFuture(cause);
		} catch(NotFoundException e) {
			return createNotFound(e);
		} catch(Throwable e) {
			return createRuntimeFuture(e);
		}			
	}

	private CompletableFuture<Action> createRuntimeFuture(Throwable e) {
		CompletableFuture<Action> future = new CompletableFuture<Action>();
		future.completeExceptionally(e);
		return future;
	}

	private CompletableFuture<Action> createNotFound(NotFoundException e) {
		CompletableFuture<Action> future = new CompletableFuture<Action>();
		future.completeExceptionally(e);
		return future;
	}
	
	@SuppressWarnings("unchecked")
	private CompletableFuture<Action> invokeMethod(MethodMeta meta) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		Method m = meta.getMethod();
		Object obj = meta.getControllerInstance();
		
		//We chose to do this here so any filters ESPECIALLY API filters 
		//can catch and translate api errors and send customers a logical response
		//On top of that ORM plugins can have a transaction filter and then in this
		//createArgs can look up the bean before applying values since it is in
		//the transaction filter
		ArgsResult argsResult = translator.createArgs(m, meta.getCtx());
		
		Object retVal = m.invoke(obj, argsResult.getArguments());
		if(retVal == null)
			throw new IllegalStateException("Your controller method returned null which is not allowed.  offending method="+m);
		
		RouteType routeType = meta.getRoute().getRouteType();
		if(routeType == RouteType.CONTENT)
			return unwrapResult(retVal, argsResult.getBinder());

		if(retVal instanceof CompletableFuture) {
			return (CompletableFuture<Action>) retVal;
		} else {
			Action action = (Action) retVal;
			return CompletableFuture.completedFuture(action);
		}
	}

	@SuppressWarnings("unchecked")
	private CompletableFuture<Action> unwrapResult(Object retVal, BodyContentBinder binder) {
		if(retVal instanceof CompletableFuture) {
			CompletableFuture<Object> future = (CompletableFuture<Object>) retVal;
			return future.thenApply((bean) -> binder.marshal(bean));
		} else {
			RenderContent content = binder.marshal(retVal);
			return CompletableFuture.completedFuture(content);
		}
	}
}
