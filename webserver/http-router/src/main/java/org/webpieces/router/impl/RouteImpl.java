package org.webpieces.router.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.api.routing.Port;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.impl.model.MatchResult;

public class RouteImpl implements Route {

	private final String path;
	private final Pattern patternToMatch;
	private final HttpMethod method;
	private final List<String> argNames;
	private final Port allowedPorts;
	private final RouteType routeType;
	private String controllerMethodString;
	private boolean checkSecureToken;
	private RouteInvoker2 routeInvoker;

	public RouteImpl(RouteInvoker2 routeInvoker, HttpMethod method, UrlPath path, String controllerMethod, RouteId routeId, Port port, boolean checkSecureToken) {
		this.routeInvoker = routeInvoker;
		this.path = path.getFullPath();
		this.method = method;
		RegExResult result = RegExUtil.parsePath(path.getSubPath());
		this.patternToMatch = Pattern.compile(result.regExToMatch);
		this.argNames = result.argNames;
		this.allowedPorts = port;
		this.controllerMethodString = controllerMethod;
		this.routeType = RouteType.HTML;
		this.checkSecureToken = checkSecureToken;
	}

	public RouteImpl(RouteInvoker2 routeInvoker, HttpMethod method, UrlPath path, String controllerMethod, Port port) {
		this.routeInvoker = routeInvoker;
		this.path = path.getFullPath();
		this.method = method;
		RegExResult result = RegExUtil.parsePath(path.getSubPath());
		this.patternToMatch = Pattern.compile(result.regExToMatch);
		this.argNames = result.argNames;
		this.allowedPorts = port;
		this.controllerMethodString = controllerMethod;
		this.routeType = RouteType.CONTENT;
		this.checkSecureToken = false;
	}
	
	public RouteImpl(RouteInvoker2 routeInvoker, String controllerMethod, RouteType routeType) {
		this.routeInvoker = routeInvoker;
		this.routeType = routeType;
		this.path = null;
		this.patternToMatch = null;
		this.method = null;
		this.argNames = new ArrayList<String>();
		this.allowedPorts = Port.BOTH;
		this.controllerMethodString = controllerMethod;
	}

	@Override
	public boolean matchesMethod(HttpMethod method) {
		if(this.method == method)
			return true;
		return false;
	}
	
	public Matcher matches(RouterRequest request, String path) {
		if(allowedPorts == Port.HTTPS && !request.isHttps) {
			//NOTE: we cannot do if isHttpsRoute != request.isHttps as every http route is 
			//allowed over https as well by default.  so 
			//isHttpsRoute=false and request.isHttps=true is allowed
			//isHttpsRoute=false and request.isHttps=false is allowed
			//isHttpsRoute=true  and request.isHttps=true is allowed
			return null; //route is https but request is http so not allowed
		} else if(this.method != request.method) {
			return null;
		}
		
		Matcher matcher = patternToMatch.matcher(path);
		return matcher;
	}

	public String getFullPath() {
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

	public RouteType getRouteType() {
		return routeType;
	}

	@Override
	public String toString() {
		return "RouteImpl [\n      path=" + path + ", \n      patternToMatch=" + patternToMatch + ", \n      method=" + method + ", \n      argNames="
				+ argNames + ", \n      isSecure=" + allowedPorts + ", \n      routeType="+routeType+"\n      controllerMethodString=" + controllerMethodString + "]";
	}

	@Override
	public boolean isPostOnly() {
		return method == HttpMethod.POST;
	}

	@Override
	public boolean isCheckSecureToken() {
		return checkSecureToken;
	}

	@Override
	public Port getExposedPorts() {
		return allowedPorts;
	}

	@Override
	public String getMethod() {
		return method+"";
	}

	@Override
	public HttpMethod getHttpMethod() {
		return method;
	}

	@Override
	public CompletableFuture<Void> invokeImpl(MatchResult result, RequestContext ctx, ResponseStreamer responseCb) {
		return routeInvoker.invokeController(result, ctx, responseCb);
	}

	@Override
	public List<String> getArgNames() {
		return this.argNames;
	}
	
}
