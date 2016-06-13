package org.webpieces.router.impl;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.Request;

public interface Route {

	String getPath();

	boolean matchesMethod(HttpMethod method);
	
	Matcher matches(Request request, String path);

	String getControllerMethodString();

	List<String> getPathParamNames();

	Set<HttpMethod> getHttpMethods();

}
