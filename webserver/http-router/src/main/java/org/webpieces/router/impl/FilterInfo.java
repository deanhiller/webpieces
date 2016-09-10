package org.webpieces.router.impl;

import java.util.regex.Pattern;

import org.webpieces.router.api.routing.RouteFilter;

public class FilterInfo<T> {

	private String path;
	private Class<? extends RouteFilter<T>> filter;
	private T initialConfig;
	private boolean isHttps;
	private Pattern patternToMatch;

	public FilterInfo(String path, Class<? extends RouteFilter<T>> filter, T initialConfig, boolean isHttps) {
		this.path = path;
		this.patternToMatch = Pattern.compile(path);
		this.filter = filter;
		this.initialConfig = initialConfig;
		this.isHttps = isHttps;
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

	public boolean isHttps() {
		return isHttps;
	}

	public Pattern getPatternToMatch() {
		return patternToMatch;
	}
	
}
