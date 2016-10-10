package org.webpieces.plugins.hsqldb;

import org.h2.tools.Server;

import com.google.inject.AbstractModule;

public class H2DbModule extends AbstractModule {

	@Override
	protected void configure() {
		String args[] = new String[] { };
		// start the TCP Server
		//Server server = Server.createWebServer(args).start();
		
		//server.start();
	}

}
