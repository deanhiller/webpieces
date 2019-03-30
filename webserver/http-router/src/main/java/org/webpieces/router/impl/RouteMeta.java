package org.webpieces.router.impl;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.BodyContentBinder;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.impl.loader.HaveRouteException;
import org.webpieces.router.impl.model.MatchResult;
import org.webpieces.router.impl.model.MatchResult2;
import org.webpieces.router.impl.model.RouteModuleInfo;
import org.webpieces.util.filters.Service;

import com.google.inject.Injector;

public class RouteMeta {

	//Different routes...
	//1. HTML with controller, method, etc.
	//2. RawContent with controller, method, etc.
	//3. Static
	//4. NotFoundRoute with controller, method, but no 
	//5. InternalErrorRoute 
	//6. Maybe a multi-route that binds one url with many different accept types creating yet another mini-router in the url
	
	private final Route route;
	private final RouteModuleInfo routeModuleInfo; //not static, only html and rawContent, notfoundRoute, internalErrorRoute
	private final Injector injector; //not static, only html and rawContent, notFoundRoute, internalErrorRoute
	private final Charset urlEncoding; //all routes that have url coming in.  ie. not NotFoundRoute nor InternalErrorRoute
	
	//loaded in configuraction phase (phase where webapp is calling addRoute) OR for dev server ONLY when about to be invoked!!!
	private Object controllerInstance; 
	private Method method;
	private List<String> methodParamNames;
	private BodyContentBinder bodyContentBinder; //only body content binder has this

	
	//loaded in build phase.  phase after configure where we call bldr.build()
	private List<FilterInfo<?>> filtersToApply = new ArrayList<>();
	private Service<MethodMeta, Action> filtersAndMethodToCall;

	public RouteMeta(Route r, Injector injector, RouteModuleInfo routerInfo, Charset urlEncoding) {
		if(routerInfo == null)
			throw new IllegalArgumentException("routerInfo must be non-null");
		this.route = r;
		this.routeModuleInfo = routerInfo;
		this.injector = injector;
		this.urlEncoding = urlEncoding;
	}

	public Route getRoute() {
		return route;
	}

	public Object getControllerInstance() {
		return controllerInstance;
	}

	public Method getMethod() {
		return method;
	}

	public void setControllerInstance(Object controllerInstance) {
		this.controllerInstance = controllerInstance;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public void setMethodParamNames(List<String> paramNames) {
		this.methodParamNames = paramNames;
	}
	
	public MatchResult2 matches2(RouterRequest request, String subPath) {
		Matcher matcher = route.matches(request, subPath);
		if(matcher == null)
			return null;
		else if(!matcher.matches())
			return null;

		List<String> names = route.getPathParamNames();
		Map<String, String> namesToValues = new HashMap<>();
		for(String name : names) {
			String value = matcher.group(name);
			if(value == null) 
				throw new IllegalArgumentException("Bug, something went wrong. request="+request);
			//convert special characters back to their normal form like '+' to ' ' (space)
			String decodedVal = urlDecode(value);
			namesToValues.put(name, decodedVal);
		}
		
		return new MatchResult2(namesToValues);
	}
	
	@Override
	public String toString() {
		return "\nRouteMeta [route=\n   " + route + ", \n   method=" + method
				+ ",\n   methodParamNames=" + methodParamNames + "]";
	}

	public String getPackageContext() {
		return routeModuleInfo.packageName;
	}

	public String getI18nBundleName() {
		return routeModuleInfo.i18nBundleName;
	}
	
	public Injector getInjector() {
		return injector;
	}
	
	private String urlDecode(Object value) {
		try {
			return URLDecoder.decode(value.toString(), urlEncoding.name());
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setFilters(List<FilterInfo<?>> filters) {
		this.filtersToApply = filters;
	}
	
	public List<FilterInfo<?>> getFilters() {
		return filtersToApply;
	}

	public void setService(Service<MethodMeta, Action> svc) {
		if(this.filtersAndMethodToCall != null)
			throw new IllegalStateException("Service was already set on this RouteMeta="+this+".  It should only be set once on startup");
		this.filtersAndMethodToCall = svc;
	}

	public Service<MethodMeta, Action> getService222() {
		return filtersAndMethodToCall;
	}

	public void setContentBinder(BodyContentBinder binder) {
		this.bodyContentBinder = binder;
	}
	
	public BodyContentBinder getBodyContentBinder() {
		return bodyContentBinder;
	}

	public CompletableFuture<Void> invoke(RequestContext ctx, ResponseStreamer responseCb,
			Map<String, String> pathParams) {
		CompletableFuture<Void> future;
		MatchResult result = new MatchResult(this, pathParams);

		try {
			future = route.invokeImpl(result, ctx, responseCb);
		} catch(Throwable e) {
			future = new CompletableFuture<Void>();
			future.completeExceptionally(e);
		}
		
		return future.handle((r, t) -> {
			if(t != null) {
				CompletableFuture<Void> future1 = new CompletableFuture<Void>();
				future1.completeExceptionally(new HaveRouteException(result, t));
				return future1;
			}
			
			return CompletableFuture.completedFuture(r); 
		}).thenCompose(Function.identity());
	}

	public CompletableFuture<Void> invokeError(RequestContext ctx, ResponseStreamer responseCb) {
		MatchResult result = new MatchResult(this);
		return route.invokeImpl(result, ctx, responseCb);
	}
}
