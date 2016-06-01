package org.webpieces.router.impl;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.webpieces.router.api.Route;
import org.webpieces.router.api.RouteId;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.Request;

import com.google.common.collect.Sets;
import com.google.inject.Injector;

public class RouteImpl implements Route {

	private final String path;
	private final Pattern patternToMatch;
	private final Set<HttpMethod> methods;
	private final List<String> argNames;
	private final boolean isSecure;

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
	}

	public RouteImpl(String controllerMethod) {
		this.path = null;
		this.patternToMatch = null;
		this.methods = null;
		this.argNames = null;
		this.isSecure = false;
		
	}

	@Override
	public boolean matchesMethod(HttpMethod method) {
		if(methods.contains(method))
			return true;
		return false;
	}
	
	public boolean matches(Request request, String path) {
		if(isSecure) {
			if(!request.isHttps)
				return false;
		}
		
		//TODO: implement more in the future, domain/host matching, header matching, etc.
		
		return patternToMatch.matcher(path).matches();
	}

	public String getPath() {
		return path;
	}

	@Override
	public Object getController(Injector injector) {
		
		return null;
	}
}
