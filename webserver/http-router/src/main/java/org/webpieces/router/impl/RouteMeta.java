package org.webpieces.router.impl;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.loader.HaveRouteException;
import org.webpieces.router.impl.loader.svc.MethodMeta;
import org.webpieces.router.impl.model.MatchResult;
import org.webpieces.router.impl.model.RouteModuleInfo;
import org.webpieces.util.filters.Service;

import com.google.inject.Injector;

public class RouteMeta extends AbstractRouteMetaImpl {

	//let's start with StaticRoute vs. DynamicRoute!!!
	//
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
	
	//loaded in configuraction phase (phase where webapp is calling addRoute) OR for dev server ONLY when about to be invoked!!!
	private Object controllerInstance; 
	private Method method;
	private List<String> methodParamNames;
	private BodyContentBinder bodyContentBinder; //only body content binder has this

	
	//loaded in build phase.  phase after configure where we call bldr.build()
	private List<FilterInfo<?>> filtersToApply = new ArrayList<>();
	private Service<MethodMeta, Action> filtersAndMethodToCall;
	private ControllerLoader controllerFinder;

	public RouteMeta(Route r, Injector injector, ControllerLoader controllerFinder, RouteModuleInfo routerInfo, Charset urlEncoding) {
		super(r, urlEncoding);
		this.controllerFinder = controllerFinder;
		if(routerInfo == null)
			throw new IllegalArgumentException("routerInfo must be non-null");
		this.route = r;
		this.routeModuleInfo = routerInfo;
		this.injector = injector;
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
				if(t instanceof NotFoundException)
					future1.completeExceptionally(t);
				else
					future1.completeExceptionally(new HaveRouteException(result, t));
				return future1;
			}
			
			return CompletableFuture.completedFuture(r); 
		}).thenCompose(Function.identity());
	}

	public void loadFiltersIntoMeta(boolean isInitializingAllFilters) {
		//NEED to move this further up
		if(getRoute().getRouteType() == RouteType.CONTENT) {
			controllerFinder.loadFiltersIntoContentMeta(this, isInitializingAllFilters);
		} else {
			throw new IllegalStateException("oops="+getRoute().getRouteType());
		}
	}

	public String getFullPath() {
		return route.getFullPath();
	}

	public Port getExposedPorts() {
		return route.getExposedPorts();
	}


}
