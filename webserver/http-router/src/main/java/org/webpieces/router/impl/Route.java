package org.webpieces.router.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.model.MatchResult;

public interface Route {

	String getFullPath();

	boolean matchesMethod(HttpMethod method);
	
	Matcher matches(RouterRequest request, String subPath);

	String getControllerMethodString();

	List<String> getPathParamNames();

	RouteType getRouteType();

	boolean isPostOnly();

	boolean isCheckSecureToken();

	Port getExposedPorts();

	HttpMethod getHttpMethod();
	
	String getMethod();

	CompletableFuture<Void> invokeImpl(MatchResult result, RequestContext ctx, ResponseStreamer responseCb, NotFoundException exc);

	List<String> getArgNames();
}
