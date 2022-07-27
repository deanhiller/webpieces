package org.webpieces.plugin.hsqldb;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.plugin.backend.spi.BackendGuiDescriptor;
import org.digitalforge.sneakythrow.SneakyThrow;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class H2DbModule extends AbstractModule {

	private static final Logger log = LoggerFactory.getLogger(H2DbModule.class);
	private H2DbConfig config;
	private ServerConfig svrConfig = new ServerConfig();

	public H2DbModule(H2DbConfig config) {
		this.config = config;
	}
	
	@Override
	protected void configure() {
		Multibinder<BackendGuiDescriptor> backendBinder = Multibinder.newSetBinder(binder(), BackendGuiDescriptor.class);
	    backendBinder.addBinding().to(H2DbGuiDescriptor.class);
	    
		bind(H2DbConfig.class).toInstance(config);
		bind(ServerConfig.class).toInstance(svrConfig);
		
		try {
			String args[];
			
			//if this port1 is 0, server.getPort will get the real port later in this method
			int port1 = config.getPort().get();
			
			if(config.getConvertDomain() == null) {
				args = new String[] { "-webPort", port1+"" };
			} else {
				//if we are converting a domain, definitely need to allow other ip addresses in..
				//this is because we are exposing a domain url on the web to hit
				args = new String[] { "-webPort", port1+"", "-webAllowOthers"};					
			}
			
			log.info("Creating H2 webserver for html GUI interface to serve up as a webpage(for development servers)");
			// start the TCP Server
			Server server = Server.createWebServer(args);
			log.info("Starting H2 webserver");
			server.start();
			
			int port = server.getPort();
			log.info("H2 webserver started on port="+port);
			if(config.getConvertDomain() == null) {
				log.info("H2 webserver setting webpage to use="+port);
				this.svrConfig.setPort(port);
			} else {
				log.info("H2 webserver using the domain converter="+config.getConvertDomain());
			}
			
			return;
		} catch(Exception e) {
			throw SneakyThrow.sneak(e);
		}
	}

}
