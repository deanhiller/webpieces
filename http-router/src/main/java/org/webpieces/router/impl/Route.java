package org.webpieces.router.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.Request;

public interface Route {

	String getPath();

	boolean matchesMethod(HttpMethod method);
	
	boolean matches(Request request, String path);

	String getControllerMethodString();

	List<String> getPathParamNames();

	Set<HttpMethod> getHttpMethods();

	Map<String, String> createParams(Request req);
}
