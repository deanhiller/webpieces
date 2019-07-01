package org.webpieces.plugins.hsqldb;

import java.util.function.Function;

public class H2DbConfig {

	private int port = 0;
	private String pluginPath = "/@db";
	private Function<String, String> convertDomain = null;

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

	/**
	 * For twitter internal use with serviceproxy.  If set, instead of using the port, to hit,
	 * we use the domain name change.. 
	 */
	public Function<String, String> getConvertDomain() {
		return convertDomain;
	}

}
