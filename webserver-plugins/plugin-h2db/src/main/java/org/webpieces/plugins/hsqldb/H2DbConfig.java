package org.webpieces.plugins.hsqldb;

public class H2DbConfig {

	private int port = 0;
	private String urlPath;

	public H2DbConfig(int port, String urlPath) {
		super();
		this.port = port;
		this.urlPath = urlPath;
	}

	public int getPort() {
		return port;
	}

	public String getUrlPath() {
		return urlPath;
	}

}
