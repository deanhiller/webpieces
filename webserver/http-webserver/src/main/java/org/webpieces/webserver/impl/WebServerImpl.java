package org.webpieces.webserver.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.frontend2.api.HttpSvrConfig;
import org.webpieces.frontend2.api.StreamListener;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.router.api.PortConfig;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.api.exceptions.RouteNotFoundException;
import org.webpieces.router.impl.compression.FileMeta;
import org.webpieces.templating.api.ProdTemplateModule;
import org.webpieces.util.cmdline2.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.util.net.URLEncoder;
import org.webpieces.webserver.api.HttpSvrInstanceConfig;
import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;

public class WebServerImpl implements WebServer {

	private static final Logger log = LoggerFactory.getLogger(WebServerImpl.class);
		
	private final WebServerConfig config;
	private final HttpFrontendManager serverMgr;
	private final RequestReceiver serverListener;
	private final RouterService routingService;
	private final WebServerPortInformation portConfig;
	private final PortConfiguration portAddresses;

	private HttpServer httpServer;
	private HttpServer httpsServer;
	private HttpServer backendServer;

	private boolean isConfigured = false;

	@Inject
	public WebServerImpl(
			WebServerConfig config,
			HttpFrontendManager serverMgr,
			RequestReceiver serverListener,
			RouterService routingService,
			WebServerPortInformation portConfig,
			PortConfiguration portAddresses
	) {
		this.config = config;
		this.serverMgr = serverMgr;
		this.serverListener = serverListener;
		this.routingService = routingService;
		this.portConfig = portConfig;
		this.portAddresses = portAddresses;
	}
	
	public void configureSync(Arguments arguments) {
		if(isConfigured)
			throw new IllegalStateException("Can't call configure twice");
		routingService.configure(arguments);
		
		isConfigured = true;
	}
	
	@Override
	public void startSync() {
		CompletableFuture<Void> future = startAsync();
		try {
			//If your server starts taking more than 2 seconds to start, your feature tests run the risk of being too
			//long.  Go back and make sure you only load what you need for the tests and keep this under 2 seconds and make
			//production use the async method!
			future.get(2, TimeUnit.SECONDS);
		} catch (Exception e) {
			throw new RuntimeException("exception", e);
		}
	}
	
	@Override
	public CompletableFuture<Void> startAsync() {
		if(!isConfigured)
			throw new IllegalStateException("You must call configure first");
		
		log.info("starting server");
		routingService.start();

		//validate html route id's and params on startup if 'org.webpieces.routeId.txt' exists
		validateRouteIdsFromHtmlFiles();

		//START http server if wanted...
		HttpSvrInstanceConfig httpConfig = config.getHttpConfig();
		InetSocketAddress http = portAddresses.getHttpAddr().get();
		CompletableFuture<Void> fut1 = startServer(http, httpConfig, "http", (config, listener, factory) -> {
			httpServer = serverMgr.createHttpServer(config, listener);
			return httpServer.start();
		});

		//START https server if wanted...
		HttpSvrInstanceConfig httpsConfig = config.getHttpsConfig();
		InetSocketAddress https = portAddresses.getHttpsAddr().get();
		CompletableFuture<Void> fut2 = startServer(https, httpsConfig, "https", (config, listener, factory) -> {
			httpsServer = serverMgr.createHttpsServer(config, listener, factory);
			return httpsServer.start();
		});

		//START backend if wanted (if not, pages are served over https server...if you don't want a backend, remove the plugins)
		HttpSvrInstanceConfig backendConfig = config.getBackendSvrConfig();
		String type = "https";
		if(backendConfig.getSslEngineFactory() == null)
			type = "http";
		InetSocketAddress backend = portAddresses.getBackendAddr().get();
		CompletableFuture<Void> fut3 = startServer(backend, backendConfig, "backend("+type+")", (config, listener, factory) -> {
			backendServer = serverMgr.createBackendHttpsServer(config, listener, factory);
			return backendServer.start();
		});
		
		return CompletableFuture.allOf(fut1, fut2, fut3).thenApply((v) -> {
			int httpPort = getUnderlyingHttpChannel().getLocalAddress().getPort();
			int httpsPort = getUnderlyingHttpsChannel().getLocalAddress().getPort();
			portConfig.setPortConfig(new PortConfig(httpPort, httpsPort));
			log.info("All servers started");	
			return null;
		});
	}

	private interface TriConsumer<T, X, Y> {
		public CompletableFuture<Void> apply(T t, X x, Y y);
	}
	
	private CompletableFuture<Void> startServer(
			InetSocketAddress bindAddress,
			HttpSvrInstanceConfig instanceConfig, 
			String serverName, 
			TriConsumer<HttpSvrConfig, StreamListener, SSLEngineFactory> function
	) {
		CompletableFuture<Void> fut3;
		if(bindAddress != null) {
			String type = "https";
			if(instanceConfig.getSslEngineFactory() == null)
				type = "http";
			
			log.info("Creating and starting the "+serverName+" over port="+bindAddress+" AND using '"+type+"'");

			HttpSvrConfig httpSvrConfig = new HttpSvrConfig(serverName, bindAddress, 10000);
			httpSvrConfig.asyncServerConfig.functionToConfigureBeforeBind = instanceConfig.getFunctionToConfigureServerSocket();
			
			fut3 = function.apply(httpSvrConfig, serverListener, instanceConfig.getSslEngineFactory());

		} else {
			fut3 = CompletableFuture.completedFuture(null);
			log.info("Serving the "+serverName+" is disabled since there was no address specified");
		}
		return fut3;
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
			loopThroughFile(url, bufReader);
		}
		log.info("Validation of routeIds complete");
	}

	private void loopThroughFile(URL url, BufferedReader bufReader) throws IOException {
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
			String location = URLEncoder.decode(split[1], StandardCharsets.UTF_8);
			String meta = split[2];

			try {
				if(ProdTemplateModule.ROUTE_TYPE.equals(type)) {
					processRoute(line, location, meta);
				} else if(ProdTemplateModule.PATH_TYPE.equals(type)) {
					processPath(url, line, location, meta);
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

	private void processPath(URL url, String line, String location, String urlPath) throws UnsupportedEncodingException {
		String path = URLEncoder.decode(urlPath, StandardCharsets.UTF_8);
		FileMeta meta = routingService.relativeUrlToHash(path);
		if(meta == null)
			throw new RouteNotFoundException("backing file for urlPath="+path+" was not found or route is missing to connect url to path.  url="+url);
	}

	private void processRoute(String line, String location, String meta) throws UnsupportedEncodingException {
		String[] split2 = meta.split(":");
		if(split2.length != 3)
			throw new IllegalStateException("size="+split2.length+" Corrupt line, wrong size="+line);
		String routeId = URLEncoder.decode(split2[0], StandardCharsets.UTF_8);
		String args = URLEncoder.decode(split2[1], StandardCharsets.UTF_8);
		
		Map<String, String> argsWithFakeValues = new HashMap<>();
		if(!"".equals(args.trim())) {
			String[] argArray = args.split(",");
			for(String arg : argArray) {
				argsWithFakeValues.put(arg.trim(), "fakeValue");
			}
		}

		log.info("validating recorded line="+line);
		routingService.convertToUrl(routeId, argsWithFakeValues, true);
	}

	@Override
	public void stop() {
		httpServer.close();
		if(httpsServer != null)
			httpsServer.close();
		if(backendServer != null)
			backendServer.close();
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
