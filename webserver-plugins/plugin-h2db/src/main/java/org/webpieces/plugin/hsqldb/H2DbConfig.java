package org.webpieces.plugin.hsqldb;

import java.util.function.Function;
import java.util.function.Supplier;

public class H2DbConfig {

	private Supplier<Integer> port = () -> 0; //default to 0 if not set
	private String pluginPath = "/@db";
	private Function<String, String> convertDomain = null;

	public H2DbConfig() {
	}
	
	public H2DbConfig(Supplier<Integer> port, String urlPath) {
		super();
		this.port = port;
		this.pluginPath = urlPath;
	}

	public Supplier<Integer> getPort() {
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

	public void setConvertDomain(Function<String, String> convertDomain) {
		this.convertDomain = convertDomain;
	}

}
