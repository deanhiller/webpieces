package org.webpieces.router.impl;

public class UrlInfo {

	private boolean isSecure;
	private int port;
	private String path;

	public UrlInfo(boolean isSecure, int port, String path) {
		this.isSecure = isSecure;
		this.port = port;
		this.path = path;
	}

	public boolean isSecure() {
		return isSecure;
	}

	public int getPort() {
		return port;
	}

	public String getPath() {
		return path;
	}
	
}
