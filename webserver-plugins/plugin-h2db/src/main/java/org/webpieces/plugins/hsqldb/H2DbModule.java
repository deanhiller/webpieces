package org.webpieces.plugins.hsqldb;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.inject.AbstractModule;

public class H2DbModule extends AbstractModule {

	private static final Logger log = LoggerFactory.getLogger(H2DbModule.class);
	private H2DbConfig config;

	public H2DbModule() {
		
			try {
				String args[] = new String[] { "-webPort", "0" };
				
				log.info("trying to start H2 webserver GUI interface to serve up as a webpage(for development servers)");
				// start the TCP Server
				Server server = Server.createWebServer(args);
				server.start();
				
				int port = server.getPort();
				log.info("H2 webserver started on port="+port);
				this.config = new H2DbConfig();
				this.config.setPort(port);
				return;
			} catch(SQLException e) {
				throw new RuntimeException(e);
			}
	}
	
	@Override
	protected void configure() {
		bind(H2DbConfig.class).toInstance(config);
	}

}
