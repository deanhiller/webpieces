package org.webpieces.webserver.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.frontend.api.HttpServer;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.exceptions.RouteNotFoundException;
import org.webpieces.router.api.routing.Nullable;
import org.webpieces.templating.api.ProdTemplateModule;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
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
	
	private HttpServer httpServer;
	private HttpServer httpsServer;

	@Override
	public RequestListener start() {
		log.info("starting server");
		routingService.start();

		//validate html route id's and params on startup if 'org.webpieces.routeId.txt' exists
		validateRouteIdsFromHtmlFiles();
		
		FrontendConfig svrChanConfig = new FrontendConfig("http", config.getHttpListenAddress());
		svrChanConfig.asyncServerConfig.functionToConfigureBeforeBind = config.getFunctionToConfigureServerSocket();
		httpServer = serverMgr.createHttpServer(svrChanConfig, serverListener);
		httpServer.start();

		if(factory != null) {
			FrontendConfig secureChanConfig = new FrontendConfig("https", config.getHttpsListenAddress(), 10000);
			secureChanConfig.asyncServerConfig.functionToConfigureBeforeBind = config.getFunctionToConfigureServerSocket();
			httpsServer = serverMgr.createHttpsServer(secureChanConfig, serverListener, factory);
			httpsServer.start();
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
			//This should be done in one unit test that boots the server to verify
			//all routes in all pages exist and have no typos
			log.info("Not validating routeIds due to configuration");
			return;
		}
		String file = "/"+ProdTemplateModule.ROUTE_META_FILE;
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

			String[] split = line.split("/");
			if(split.length != 3)
				throw new IllegalStateException("size="+split.length+" corrupt line="+line);
			
			String type = split[0];
			String location = URLDecoder.decode(split[1], StandardCharsets.UTF_8.name());
			String meta = split[2];

			try {
				if(ProdTemplateModule.ROUTE_TYPE.equals(type)) {
					processRoute(line, location, meta);
				} else if(ProdTemplateModule.PATH_TYPE.equals(type)) {
					processPath(line, location, meta);
				} else 
					throw new IllegalStateException("wrong type.  corrupt line="+line);				
			} catch(RouteNotFoundException e) {
				if(firstException == null)
					firstException = e;
				
				errorMsg += "\n\nError "+(count++) + ": "+e.getMessage() +" location="+location+"\n entire line="+line; 
			}
		}
		
		if(firstException != null)
			throw new RuntimeException("There were one or more invalid routeIds in html files="+errorMsg, firstException);
	}

	private void processPath(String line, String location, String urlPath) throws UnsupportedEncodingException {
		String path = URLDecoder.decode(urlPath, StandardCharsets.UTF_8.name());
		String hash = routingService.relativeUrlToHash(path);
		if(hash == null)
			throw new RouteNotFoundException("backing file for urlPath="+path+" was not found or route is missing to connect url to path");
	}

	private void processRoute(String line, String location, String meta) throws UnsupportedEncodingException {
		String[] split2 = meta.split(":");
		if(split2.length != 3)
			throw new IllegalStateException("size="+split2.length+" Corrupt line, wrong size="+line);
		String routeId = URLDecoder.decode(split2[0], StandardCharsets.UTF_8.name());
		String args = URLDecoder.decode(split2[1], StandardCharsets.UTF_8.name());
		
		Map<String, String> argsWithFakeValues = new HashMap<>();
		if(!"".equals(args.trim())) {
			String[] argArray = args.split(",");
			for(String arg : argArray) {
				argsWithFakeValues.put(arg.trim(), "fakeValue");
			}
		}

		log.info("validating recorded line="+line);
		routingService.convertToUrl(routeId, argsWithFakeValues);
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
