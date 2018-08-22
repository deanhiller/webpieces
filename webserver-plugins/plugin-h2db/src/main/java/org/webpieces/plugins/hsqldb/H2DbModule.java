package org.webpieces.plugins.hsqldb;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.webpieces.plugins.backend.spi.BackendGuiDescriptor;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class H2DbModule extends AbstractModule {

	private static final Logger log = LoggerFactory.getLogger(H2DbModule.class);
	private H2DbConfig config;
	private ServerConfig svrConfig = new ServerConfig();

	public H2DbModule(H2DbConfig config) {
		
			try {
				this.config = config;
				String args[] = new String[] { "-webPort", config.getPort()+"" };
				
				log.info("trying to start H2 webserver GUI interface to serve up as a webpage(for development servers)");
				// start the TCP Server
				Server server = Server.createWebServer(args);
				server.start();
				
				int port = server.getPort();
				log.info("H2 webserver started on port="+port);
				this.svrConfig.setPort(port);
				return;
			} catch(SQLException e) {
				throw new RuntimeException(e);
			}
	}
	
	@Override
	protected void configure() {
		Multibinder<BackendGuiDescriptor> backendBinder = Multibinder.newSetBinder(binder(), BackendGuiDescriptor.class);
	    backendBinder.addBinding().to(H2DbGuiDescriptor.class);
	    
		bind(H2DbConfig.class).toInstance(config);
		bind(ServerConfig.class).toInstance(svrConfig);
	}

}
