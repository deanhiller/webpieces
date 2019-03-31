package org.webpieces.router.impl;

import java.util.List;
import java.util.regex.Matcher;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.routes.Port;

public interface BasicRoute {

	Matcher matches(RouterRequest request, String subPath);

	List<String> getPathParamNames();

	String getFullPath();

	Port getExposedPorts();

	String getMethod();

}
