package org.webpieces.router.api.routes;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.util.cmdline2.Arguments;

public class WebAppConfig {

	private Arguments cmdLineArguments;
	private Map<String, Object> properties = new HashMap<String, Object>();

	public WebAppConfig(Arguments cmdLineArguments, Map<String, Object> properties) {
		super();
		this.cmdLineArguments = cmdLineArguments;
		this.properties = properties;
	}

	public Arguments getCmdLineArguments() {
		return cmdLineArguments;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}
	
}
