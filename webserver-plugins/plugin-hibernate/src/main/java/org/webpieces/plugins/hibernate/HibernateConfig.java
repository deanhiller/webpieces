package org.webpieces.plugins.hibernate;

import org.webpieces.util.cmdline2.Arguments;

public class HibernateConfig {

	private Arguments cmdLineArguments;

	public HibernateConfig(Arguments cmdLineArgs) {
		super();
		this.cmdLineArguments = cmdLineArgs;
	}

	public Arguments getCmdLineArguments() {
		return cmdLineArguments;
	}

}
