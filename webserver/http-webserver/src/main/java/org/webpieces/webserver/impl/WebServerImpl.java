package org.webpieces.webserver.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontend;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.exceptions.RouteNotFoundException;
import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;

public class WebServerImpl implements WebServer {

	private static final Logger log = LoggerFactory.getLogger(WebServerImpl.class);
	
	@Inject
	private WebServerConfig config;
	
	@Inject @Nullable
	private SSLEngineFactory factory;
	@Inject
	private HttpFrontendManager serverMgr;
	@Inject
	private RequestReceiver serverListener;
	@Inject
	private RoutingService routingService;
	
	private HttpFrontend httpServer;
	private HttpFrontend httpsServer;

	@Override
	public HttpRequestListener start() {
		log.info("starting server");

		routingService.start();

		//validate html route id's and params on startup if 'org.webpieces.routeId.txt' exists
		validateRouteIdsFromHtmlFiles();
		
		FrontendConfig svrChanConfig = new FrontendConfig("http", config.getHttpListenAddress());
		svrChanConfig.asyncServerConfig.functionToConfigureBeforeBind = config.getFunctionToConfigureServerSocket();
		httpServer = serverMgr.createHttpServer(svrChanConfig, serverListener);
		
		if(factory != null) {
			FrontendConfig secureChanConfig = new FrontendConfig("https", config.getHttpsListenAddress());
			secureChanConfig.asyncServerConfig.functionToConfigureBeforeBind = config.getFunctionToConfigureServerSocket();
			httpsServer = serverMgr.createHttpsServer(secureChanConfig, serverListener, factory);
		} else {
			log.info("https port is disabled since configuration had no sslEngineFactory");
		}
		
		log.info("server started");
		return serverListener;
	}

	private void validateRouteIdsFromHtmlFiles() {
		try {
			validateRouteIdsFromHtmlFilesImpl();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void validateRouteIdsFromHtmlFilesImpl() throws IOException {
		if(!config.isValidateRouteIdsOnStartup()) {
			log.info("Not validating routeIds");
			return;
		}
		String file = "/org.webpieces.routeId.txt";
		log.info("Valiating routeIds from the recorded file="+file.substring(1));
		
		URL url = getClass().getResource(file);
		log.info("file found at location="+url);
		if(url == null) {
			throw new IllegalArgumentException("File not found on classpath="+file);
		}
		
		try (InputStream in = url.openStream();
			 InputStreamReader reader = new InputStreamReader(in);
			 BufferedReader bufReader = new BufferedReader(reader)) {
			loopThroughFile(bufReader);
		}
		log.info("Validation of routeIds complete");
	}

	private void loopThroughFile(BufferedReader bufReader) throws IOException {
		RouteNotFoundException firstException = null;
		int count = 1;
		String errorMsg = "";
		String line;
		while((line=bufReader.readLine())!=null) {
			if("".equals(line.trim()))
				continue;
			
			String[] split = line.split(":");
			String routeId = split[0];
			String args = split[1];
			String location = split[2];
			
			Map<String, String> argsWithFakeValues = new HashMap<>();
			if(!"".equals(args.trim())) {
				String[] argArray = args.split(",");
				for(String arg : argArray) {
					argsWithFakeValues.put(arg, "fakeValue");
				}
			}
		
			try {
				log.info("validating recorded line="+line);
				routingService.convertToUrl(routeId, argsWithFakeValues);
			} catch(RouteNotFoundException e) {
				if(firstException == null)
					firstException = e;
				
				errorMsg += "\n\nError "+(count++) + ": "+e.getMessage() +" location="+location; 
			}
		}
		
		if(firstException != null)
			throw new RuntimeException("There were one or more invalid routeIds in html files="+errorMsg, firstException);
	}

	@Override
	public void stop() {
		httpServer.close();
		if(httpsServer != null)
			httpsServer.close();
	}

	@Override
	public TCPServerChannel getUnderlyingHttpChannel() {
		return httpServer.getUnderlyingChannel();
	}
	
	@Override
	public TCPServerChannel getUnderlyingHttpsChannel() {
		return httpsServer.getUnderlyingChannel();
	}

}
