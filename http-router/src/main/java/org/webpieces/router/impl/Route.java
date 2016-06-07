package org.webpieces.router.impl;

import java.util.List;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.Request;

public interface Route {

	String getPath();

	boolean matchesMethod(HttpMethod method);
	
	boolean matches(Request request, String path);

	String getControllerMethodString();

	List<String> getArgNames();
}
