package org.webpieces.router.api.dto;

public class Request {

	public boolean isHttps;
	public String relativePath;
	/**
	 * This comes from sniServerName in the case of https or Host header in case of http but even
	 * Host in https should match
	 */
	public String domain;
	public HttpMethod method;
	
}
