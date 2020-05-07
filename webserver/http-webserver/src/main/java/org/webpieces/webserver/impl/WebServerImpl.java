package org.webpieces.webserver.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.frontend2.api.HttpSvrConfig;
import org.webpieces.nio.api.SSLConfiguration;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.router.api.PortConfig;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.api.exceptions.RouteNotFoundException;
import org.webpieces.router.impl.compression.FileMeta;
import org.webpieces.templating.api.ProdTemplateModule;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.net.URLEncoder;
import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;

import com.google.inject.Injector;

@Singleton
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
	
	/**
	 * EVERY server has a few modes.  The only exception is the http server will not let you set the sslEngineFactory
	 * or it will throw an exception since it doesn't do SSL.
	 * 
	 * Mode 1: listenAddress=null sslEngineFactory=null - server disabled
	 * Mode 2: listenAddress=null sslEngineFactory=YourSSLEngineFactory - server disabled
	 * Mode 3: listenAddress={some port} sslEngineFactory=null - server will be started and will serve http.  This means
	 *         for the https server, it WILL serve http and your firewall can hit port 443 without the need of the
	 *         x-forwarded-proto.  The backend server will also serve http in this mode over it's port
	 * Mode 4: listenAddress={some port} sslEngineFactory=YourSSLEngineFactory - server will be started and will serve
	 *         https.  If you try this on the http server, it will throw an exception.
	 *         
	 * Some notes though.  You can terminate SSL at the firewall, and route to your http port for https as long as
	 * you set the x-forwarded-proto to https.  This way, https pages are served through your http port BUT only for
	 * those requests that terminated ssl on your firewall.  (not a good idea though if you are transferring credit
	 * card information so I typically don't do that).
	 * 
	 * The backend server is a bit special.  If you disable it by not having the listenAddress set, those pages will
	 * be served over the https server.  If you disable both https and http, the only way you can access https and
	 * backend pages is by setting the x-forwarded-proto to https.
	 * 
	 * Because webpieces allows you to load certificates from a database, we like the idea of just terminating SSL 
	 * on the webpieces server itself.  All servers in your cluster can load the one certificate in the database
	 * and you can change the certificate in the database to get all servers to update.
	 * 
	 * @author dhiller
	 *
	 */
	@Override
	public CompletableFuture<Void> startAsync() {
		if(!isConfigured)
			throw new IllegalStateException("You must call configure first");
		
		log.info("starting server");
		Injector injector = routingService.start();
		
		//validate html route id's and params on startup if 'org.webpieces.routeId.txt' exists
		validateRouteIdsFromHtmlFiles();

		//START http server if wanted...
		InetSocketAddress httpAddress = portAddresses.getHttpAddr().get();
		boolean httpsOverHttpEnabled = portAddresses.getAllowHttpsIntoHttp().get();
		CompletableFuture<Void> fut1;
		if(httpAddress != null) {
			fut1 = startServerOnHttpPort(injector, httpAddress, httpsOverHttpEnabled);
		} else {
			fut1 = CompletableFuture.completedFuture(null);
			log.info("Serving the "+"http"+" is disabled since there was no address specified");
		}
		
		

		//START https server if wanted...
		InetSocketAddress httpsAddress = portAddresses.getHttpsAddr().get();
		CompletableFuture<Void> fut2;
		if(httpsAddress != null) {
			fut2 = startHttpsServer(injector, httpsAddress);
		} else {
			fut2 = CompletableFuture.completedFuture(null);
			log.info("Serving the "+"https"+" is disabled since there was no address specified");
		}

		
		
		//START backend if wanted (if not, pages are served over https server...if you don't want a backend, remove the plugins)

		InetSocketAddress backendAddress = portAddresses.getBackendAddr().get();
		CompletableFuture<Void> fut33;
		if(backendAddress != null) {
			fut33 = startBackendServer(injector, backendAddress);
		} else {
			fut33 = CompletableFuture.completedFuture(null);
			log.info("Serving the backend over it's own port is disabled since there was no address specified");
		}
		CompletableFuture<Void> fut3 = fut33;
		
		return CompletableFuture.allOf(fut1, fut2, fut3).thenApply((v) -> {
			int httpPort = -1;
			int httpsPort = -1;

			if(httpServer != null) //happens if port disabled
				httpPort = getUnderlyingHttpChannel().getLocalAddress().getPort();
			if(httpsServer != null) //happens if port disabled
				httpsPort = getUnderlyingHttpsChannel().getLocalAddress().getPort();
			
			portConfig.setPortConfig(new PortConfig(httpPort, httpsPort));
			log.info("All servers started");	
			return null;
		});
	}

	private CompletableFuture<Void> startBackendServer(Injector injector, InetSocketAddress backendAddress) {
		CompletableFuture<Void> fut33;
		//This is inside the if statement BECAUSE we do not need to bind an SSLConfiguration if they do not
		//enable the backend or https ports
		SSLConfiguration sslConfiguration = injector.getInstance(SSLConfiguration.class);

		String type = "https";
		if(sslConfiguration.getBackendSslEngineFactory() == null)
			type = "http";
		String serverName = "backend."+type;
		
		log.info("Creating and starting the "+serverName+" over port="+backendAddress+" AND using '"+type+"'");

		HttpSvrConfig httpSvrConfig = new HttpSvrConfig(serverName, backendAddress, 10000);
		httpSvrConfig.asyncServerConfig.functionToConfigureBeforeBind = s -> configure(s);
		
		SSLEngineFactory factory = (SSLEngineFactory) sslConfiguration.getBackendSslEngineFactory();
		backendServer = serverMgr.createBackendHttpsServer(httpSvrConfig, serverListener, factory);
		fut33 = backendServer.start();
		return fut33;
	}

	private CompletableFuture<Void> startHttpsServer(Injector injector, InetSocketAddress httpsAddress) {
		CompletableFuture<Void> fut2;
		//This is inside the if statement BECAUSE we do not need to bind an SSLConfiguration if they do not
		//enable the backend or https ports
		SSLEngineFactory factory = fetchSSLEngineFactory(injector);

		String type = "https";
		log.info("Creating and starting https over port="+httpsAddress+" AND using '"+type+"'");

		HttpSvrConfig httpSvrConfig = new HttpSvrConfig(type, httpsAddress, 10000);
		httpSvrConfig.asyncServerConfig.functionToConfigureBeforeBind = s -> configure(s);
		
		httpsServer = serverMgr.createHttpsServer(httpSvrConfig, serverListener, factory);
		fut2 = httpsServer.start();
		return fut2;
	}

	private CompletableFuture<Void> startServerOnHttpPort(Injector injector, InetSocketAddress httpAddress,
			boolean httpsOverHttpEnabled) {
		CompletableFuture<Void> fut1;
		String type;
		if(httpsOverHttpEnabled) {
			type = "both";
			//This is inside the if statement BECAUSE we do not need to bind an SSLConfiguration if they do not
			//enable the backend or https ports
			SSLEngineFactory factory = fetchSSLEngineFactory(injector);

			HttpSvrConfig httpSvrConfig = new HttpSvrConfig(type, httpAddress, 10000);
			httpSvrConfig.asyncServerConfig.functionToConfigureBeforeBind = s -> configure(s);
			httpServer = serverMgr.createUpgradableServer(httpSvrConfig, serverListener, factory);
		} else {
			type = "http";
			HttpSvrConfig httpSvrConfig = new HttpSvrConfig(type, httpAddress, 10000);
			httpSvrConfig.asyncServerConfig.functionToConfigureBeforeBind = s -> configure(s);
			httpServer = serverMgr.createHttpServer(httpSvrConfig, serverListener);
		}

		log.info("Created and now starting the '"+type+"' over port="+httpAddress+"  'both' means this port supports both http and https");

		fut1 = httpServer.start();
		return fut1;
	}

	private SSLEngineFactory fetchSSLEngineFactory(Injector injector) {
		SSLConfiguration sslConfiguration = injector.getInstance(SSLConfiguration.class);

		if(sslConfiguration.getHttpsSslEngineFactory() == null)
			throw new IllegalArgumentException("sslConfiguration.getHttpsSslEngineFactory() was null.  Can't setup https on this port");
		return (SSLEngineFactory) sslConfiguration.getHttpsSslEngineFactory();
	}

	/**
	 * This is a bit clunky BUT if jdk authors add methods that you can configure, we do not have
	 * to change our platform every time so you can easily set the new properties rather than waiting for
	 * us to release a new version 
	 */
	public static void configure(ServerSocketChannel channel) throws SocketException {
		channel.socket().setReuseAddress(true);
		//channel.socket().setSoTimeout(timeout);
		//channel.socket().setReceiveBufferSize(size);
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
		
		Map<String, Object> argsWithFakeValues = new HashMap<>();
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
		if(httpServer == null)
			return null;
		return httpServer.getUnderlyingChannel();
	}
	
	@Override
	public TCPServerChannel getUnderlyingHttpsChannel() {
		if(httpsServer == null)
			return null;
		return httpsServer.getUnderlyingChannel();
	}

}
