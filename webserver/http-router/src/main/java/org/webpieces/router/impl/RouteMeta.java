package org.webpieces.router.impl;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.webpieces.ctx.api.RouterRequest;

import com.google.inject.Injector;

public class RouteMeta {

	private final Route route;
	private Object controllerInstance;
	private Method method;
	private List<String> methodParamNames;
	//The package for the RouteModule for context(so controllers are relative to that module)
	private String packageContext;
	private Injector injector;
	private Charset urlEncoding;

	public RouteMeta(Route r, Injector injector, String packageContext, Charset urlEncoding) {
		this.route = r;
		this.packageContext = packageContext;
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

	public MatchResult matches(RouterRequest request, String path) {
		Matcher matcher = route.matches(request, path);
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
		return packageContext;
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
	
}
