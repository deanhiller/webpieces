package org.webpieces.router.impl;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.util.filters.Service;

import com.google.inject.Injector;

public class RouteMeta {

	private final Route route;
	private RouteModuleInfo routeModuleInfo;
	private Injector injector;
	private Charset urlEncoding;
	private List<FilterInfo<?>> filtersToApply = new ArrayList<>();

	private Service<MethodMeta, Action> filtersAndMethodToCall;
	private Object controllerInstance;
	private Method method;
	private List<String> methodParamNames;
	
	public RouteMeta(Route r, Injector injector, RouteModuleInfo routerInfo, Charset urlEncoding) {
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

	public MatchResult matches(RouterRequest request, String subPath) {
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
		
		return new MatchResult(this, namesToValues);
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
}
