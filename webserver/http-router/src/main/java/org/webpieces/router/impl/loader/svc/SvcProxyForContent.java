package org.webpieces.router.impl.loader.svc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.impl.params.ParamToObjectTranslatorImpl;
import org.webpieces.util.filters.Service;

public class SvcProxyForContent implements Service<MethodMeta, Action> {

	private ParamToObjectTranslatorImpl translator;
	private ServiceInvoker invoker;
	
	public SvcProxyForContent(ParamToObjectTranslatorImpl translator, ServiceInvoker invoker) {
		this.translator = translator;
		this.invoker = invoker;
	}
	
	@Override
	public CompletableFuture<Action> invoke(MethodMeta meta) {
		try {
			return invokeMethod(meta);
		} catch(InvocationTargetException e) {
			//DAMN these damn InvocationTargetExceptions that just fucking wrap the original
			//GET rid of checked exceptions....in reality InvocationTargetException == FUCKING ANYTHING!!!
			return invoker.createRuntimeFuture(e.getCause());
		} catch(Throwable e) {
			//IT's hard to say for Content routes like a json api, etc. what we should return on 
			//failure.  They should install a filter that captures their own failures and if not,
			//we will actually send back the error html page as this throws up to the Router file
			//that will catch and call the internalErrorRoute for html
			return invoker.createRuntimeFuture(e);
		}			
	}

	private CompletableFuture<Action> invokeMethod(MethodMeta meta) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		RouteInfoForContent info = (RouteInfoForContent) meta.getRoute();
		
		Method m = meta.getLoadedController2().getMethod();
		
		//We chose to do this here so any filters ESPECIALLY API filters 
		//can catch and translate api errors and send customers a logical response
		//On top of that ORM plugins can have a transaction filter and then in this
		//createArgs can look up the bean before applying values since it is in
		//the transaction filter
		List<Object> argsResult = translator.createArgs(m, meta.getCtx(), info.getBodyContentBinder());
		
		Object retVal = invoker.invokeController(meta.getLoadedController2(), argsResult.toArray());
		
		if(info.getBodyContentBinder() != null)
			return unwrapResult(m, retVal, info.getBodyContentBinder());

		return invoker.coerceReturnValue(retVal);
	}

	@SuppressWarnings("unchecked")
	private CompletableFuture<Action> unwrapResult(Method method, Object retVal, BodyContentBinder binder) {
		Class<?> returnType = method.getReturnType();
		
		if(CompletableFuture.class.isAssignableFrom(returnType)) {
			if(retVal == null)
				throw new IllegalStateException("Your method returned a null CompletableFuture which it not allowed.  method="+method);
			CompletableFuture<Object> future = (CompletableFuture<Object>) retVal;
			return future.thenApply((bean) -> binder.marshal(bean));
		} else {
			RenderContent content = binder.marshal(retVal);
			return CompletableFuture.completedFuture(content);
		}
	}
}
