package org.webpieces.plugins.backend;

import org.webpieces.util.cmdline2.Arguments;

public class BackendConfig {

	private Arguments arguments;

	public BackendConfig(Arguments arguments) {
		this.arguments = arguments;
	}

	public Arguments getArguments() {
		return arguments;
	}

}
