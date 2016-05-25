package com.webpieces.fullwebserver;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.RouteModule;

import com.webpieces.webserver.api.WebServer;
import com.webpieces.webserver.api.WebServerConfig;
import com.webpieces.webserver.api.WebServerFactory;

public class WebServerMain {
	private static final Logger log = LoggerFactory.getLogger(WebServerMain.class);
	
	public static void main(String[] args) {
		
		try {
			List<RouteModule> routeModules = new ArrayList<>();
			routeModules.add(new ExampleRouteModule());
			
			WebServerConfig config = new WebServerConfig();
			WebServer server = WebServerFactory.create("webserver", null, config);
			
			server.start();
		} catch(Exception e) {
			log.warn("excpeiton", e);
		}
	}
}
