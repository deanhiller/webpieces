package org.webpieces.webserver.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Supplier;

import javax.inject.Named;
import javax.inject.Singleton;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.frontend2.api.HttpFrontendFactory;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.templating.api.ConverterLookup;
import org.webpieces.templating.api.RouterLookup;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.threading.NamedThreadFactory;
import org.webpieces.util.time.Time;
import org.webpieces.util.time.TimeImpl;
import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.http2engine.api.client.InjectionConfig;

public class WebServerModule implements Module {

	public static final String HTTP_PORT_KEY = "http.port";
	public static final String HTTPS_PORT_KEY = "https.port";
	
	//3 pieces consume this key to make it work :( and all 3 pieces do NOT depend on each other so
	//this key is copied in 3 locations
	public static final String BACKEND_PORT_KEY = "backend.port";
	
	private final WebServerConfig config;
	private final Supplier<InetSocketAddress> httpAddress;
	private final Supplier<InetSocketAddress> httpsAddress;
	private final Supplier<InetSocketAddress> backendAddress;
	private final WebServerPortInformation portLookup;

	public WebServerModule(WebServerConfig config, WebServerPortInformation portLookup, Arguments args) {
		this.config = config;
		this.portLookup = portLookup;
		
		//this is too late, have to do in the Guice modules
		httpAddress = args.createOptionalInetArg(HTTP_PORT_KEY, ":8080", "Http host&port.  syntax: {host}:{port} or just :{port} to bind to all NIC ips on that host");
		httpsAddress = args.createOptionalInetArg(HTTPS_PORT_KEY, ":8443", "Http host&port.  syntax: {host}:{port} or just :{port} to bind to all NIC ips on that host");
		backendAddress = args.createOptionalInetArg(BACKEND_PORT_KEY, null, "Http(s) host&port for backend.  syntax: {host}:{port} or just :{port}.  Also, null means put the pages on the https/http ports");
	}
	
	@Override
	public void configure(Binder binder) {
		binder.bind(WebServer.class).to(WebServerImpl.class);

		binder.bind(WebServerConfig.class).toInstance(config);
		
		binder.bind(RouterLookup.class).to(RouterLookupProxy.class).asEagerSingleton();
		
		binder.bind(ConverterLookup.class).to(ConverterLookupProxy.class).asEagerSingleton();
		
		binder.bind(BufferPool.class).to(BufferCreationPool.class).asEagerSingleton();
		
		binder.bind(Time.class).to(TimeImpl.class).asEagerSingleton();
		
		//what the webserver writes to
		binder.bind(WebServerPortInformation.class).toInstance(portLookup);

		//in webpieces modules, you can't read until a certain phase :( :( so we can't read them here
		//like we can in app modules and in plugins!!
		binder.bind(PortConfiguration.class).toInstance(new PortConfiguration(httpAddress, httpsAddress, backendAddress));
	}

	@Provides
	@Singleton
	@Named(HttpFrontendFactory.FILE_READ_EXECUTOR)
	public ExecutorService provideExecutor() {
		return Executors.newFixedThreadPool(10, new NamedThreadFactory("fileReadCallbacks"));
	}
	
	@Provides
	@Singleton
	public ScheduledExecutorService provideTimer() {
		return new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("webpieces-timer"));
	}
	
	@Provides
	@Singleton
	@Named(HttpFrontendFactory.HTTP2_ENGINE_THREAD_POOL)
	public Executor providesEngineThreadPool(WebServerConfig config) {
		return Executors.newFixedThreadPool(config.getHttp2EngineThreadCount(), new NamedThreadFactory("http2Engine"));
	}
	
	@Provides
	@Singleton
	public ChannelManager providesChanMgr(WebServerConfig config, BufferPool pool) {
		String id = "webpiecesThreadPool";
		Executor executor = Executors.newFixedThreadPool(config.getNumFrontendServerThreads(), new NamedThreadFactory(id));
		
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager chanMgr = factory.createMultiThreadedChanMgr("selectorThread", pool, config.getBackpressureConfig(), executor);
		
		return chanMgr;
	}
	
	@Provides
	@Singleton
	public HttpFrontendManager providesAsyncServerMgr(
			ChannelManager chanMgr, 
			ScheduledExecutorService timer, 
			@Named(HttpFrontendFactory.HTTP2_ENGINE_THREAD_POOL) Executor executor1, 
			BufferPool pool,
			Time time, 
			WebServerConfig config) {
		
		HttpParser httpParser = HttpParserFactory.createParser(pool);
		HpackParser http2Parser = HpackParserFactory.createParser(pool, true);
		InjectionConfig injConfig = new InjectionConfig(http2Parser, time, config.getHttp2Config());

		return HttpFrontendFactory.createFrontEnd(chanMgr, timer, injConfig, httpParser);
	}
	
}
