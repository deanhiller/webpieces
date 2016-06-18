package org.webpieces.router.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.api.routing.RouteId;

import com.google.common.collect.Sets;

public class RouteImpl implements Route {

	private final String path;
	private final Pattern patternToMatch;
	private final Set<HttpMethod> methods;
	private final List<String> argNames;
	private final boolean isSecure;
	private String controllerMethodString;

	public RouteImpl(HttpMethod method, String path, String controllerMethod, RouteId routeId, boolean isSecure) {
		this(Sets.newHashSet(method), path, controllerMethod, routeId, isSecure);
	}
	
	public RouteImpl(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId, boolean isSecure) {
		this.path = path;
		this.methods = methods;
		RegExResult result = RegExUtil.parsePath(path);
		this.patternToMatch = Pattern.compile(result.regExToMatch);
		this.argNames = result.argNames;
		this.isSecure = isSecure;
		this.controllerMethodString = controllerMethod;
	}

	public RouteImpl(String controllerMethod) {
		this.path = null;
		this.patternToMatch = null;
		this.methods = new HashSet<>();
		this.argNames = new ArrayList<String>();
		this.isSecure = false;
		this.controllerMethodString = controllerMethod;
	}

	@Override
	public boolean matchesMethod(HttpMethod method) {
		if(methods.contains(method))
			return true;
		return false;
	}
	
	public Matcher matches(Request request, String path) {
		if(isSecure) {
			if(!request.isHttps)
				return null;
		} else if(!methods.contains(request.method)) {
			return null;
		}
		
		Matcher matcher = patternToMatch.matcher(path);
		return matcher;
	}

	public String getPath() {
		return path;
	}

	@Override
	public String getControllerMethodString() {
		return controllerMethodString;
	}
	
	@Override
	public List<String> getPathParamNames() {
		return argNames;
	}

	@Override
	public Set<HttpMethod> getHttpMethods() {
		return methods;
	}

	@Override
	public String toString() {
		return "Route [path=" + path + ", \npatternToMatch=" + patternToMatch + ", \nmethods=" + methods + ", \nargNames="
				+ argNames + ", \nisSecure=" + isSecure + ", \ncontrollerMethodString=" + controllerMethodString + "]";
	}
	
}
