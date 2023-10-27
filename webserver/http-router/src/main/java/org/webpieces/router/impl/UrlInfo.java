package org.webpieces.router.impl;

public class UrlInfo {

	private boolean isSecure;
	private Integer port;
	private String path;

	public UrlInfo(boolean isSecure, Integer port, String path) {
		this.isSecure = isSecure;
		this.port = port;
		this.path = path;
	}

	public boolean isSecure() {
		return isSecure;
	}

	public Integer getPort() {
		return port;
	}

	public String getPath() {
		return path;
	}
	
}
