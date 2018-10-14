package org.webpieces.plugins.hsqldb;

public class H2DbConfig {

	private int port = 0;
	private String pluginPath = "/@db";

	public H2DbConfig() {
	}
	
	public H2DbConfig(int port, String urlPath) {
		super();
		this.port = port;
		this.pluginPath = urlPath;
	}

	public int getPort() {
		return port;
	}

	public String getPluginPath() {
		return pluginPath;
	}

}
