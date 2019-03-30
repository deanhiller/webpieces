package org.webpieces.router.impl;

import java.util.regex.Pattern;

import org.webpieces.router.api.routing.PortType;
import org.webpieces.router.api.routing.RouteFilter;

public class FilterInfo<T> {

	private String path;
	private Class<? extends RouteFilter<T>> filter;
	private T initialConfig;
	private Pattern patternToMatch;
	private PortType portType;

	public FilterInfo(String path, Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type) {
		this.path = path;
		this.patternToMatch = Pattern.compile(path);
		this.filter = filter;
		this.initialConfig = initialConfig;
		this.portType = type;
	}

	public String getPath() {
		return path;
	}

	public Class<? extends RouteFilter<T>> getFilter() {
		return filter;
	}

	public T getInitialConfig() {
		return initialConfig;
	}

	public Pattern getPatternToMatch() {
		return patternToMatch;
	}

	public PortType getPortType() {
		return portType;
	}

	public boolean securityMatch(boolean isHttps) {
		if(portType == PortType.ALL_FILTER)
			return true;
		else if(isHttps && portType == PortType.HTTPS_FILTER)
			return true;
		else if(!isHttps && portType == PortType.HTTP_FILTER)
			return true;
		
		return false;
	}

	@Override
	public String toString() {
		return "FilterInfo [path=" + path + ", filter=" + filter + ", portType=" + portType + "]";
	}
	
}
