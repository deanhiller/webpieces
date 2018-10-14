package org.webpieces.webserver.api;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.handlers.ConsumerFunc;

import com.google.inject.Module;
import com.webpieces.http2engine.api.client.Http2Config;

public class WebServerConfig {

	private InetSocketAddress httpListenAddress;
	private InetSocketAddress httpsListenAddress;
	//typically only true during a test, but could be done before production server runs as well though it will slow down startup time
	private boolean validateRouteIdsOnStartup = false;
	
	private int numFrontendServerThreads = 20;
	private int http2EngineThreadCount = 20;

	/**
	 * Generally not needed by clients but we use this to overide certain objects for a development
	 * server that is optimized for development but would run slower in production such that users
	 * don't have to reboot the server and rather can make all the changes they want to code and it
	 * gets recompiled by the eclipse compiler on-demand while the server stays running
	 */
	private Module platformOverrides = null;

	/**
	 * If not set, we will not open the SSL port for the webserver
	 */
	private SSLEngineFactory sslEngineFactory;
	
	private ConsumerFunc<ServerSocketChannel> functionToConfigureServerSocket;
	private Locale defaultLocale = Locale.getDefault();
	
	//On startup, we protect developers from breaking clients.  In http, all files that change
	//must also change the hash url param automatically and the %%{ }%% tag generates those hashes so the
    //files loaded are always the latest
	//this is what gets put in the cache header for static files...and should be set to the max
	private Long staticFileCacheTimeSeconds = TimeUnit.SECONDS.convert(255, TimeUnit.DAYS);
	
	private Http2Config http2Config = new Http2Config();
	private BackpressureConfig backpressureConfig = new BackpressureConfig();
	
	private Charset defaultFormAcceptEncoding = StandardCharsets.UTF_8;

	public int getNumFrontendServerThreads() {
		return numFrontendServerThreads ;
	}

	public Module getPlatformOverrides() {
		return platformOverrides;
	}

	public WebServerConfig setNumFrontendServerThreads(int numFrontendServerThreads) {
		this.numFrontendServerThreads = numFrontendServerThreads;
		return this;
	}

	public WebServerConfig setPlatformOverrides(Module platformOverrides) {
		this.platformOverrides = platformOverrides;
		return this;
	}

	public SSLEngineFactory getSslEngineFactory() {
		return sslEngineFactory;
	}

	public WebServerConfig setSslEngineFactory(SSLEngineFactory sslEngineFactory) {
		this.sslEngineFactory = sslEngineFactory;
		return this;
	}

	public InetSocketAddress getHttpListenAddress() {
		return httpListenAddress;
	}

	public WebServerConfig setHttpListenAddress(InetSocketAddress httpListenAddress) {
		this.httpListenAddress = httpListenAddress;
		return this;
	}

	public InetSocketAddress getHttpsListenAddress() {
		return httpsListenAddress;
	}

	public WebServerConfig setHttpsListenAddress(InetSocketAddress httpsListenAddress) {
		this.httpsListenAddress = httpsListenAddress;
		return this;
	}

	public ConsumerFunc<ServerSocketChannel> getFunctionToConfigureServerSocket() {
		return functionToConfigureServerSocket;
	}

	public WebServerConfig setFunctionToConfigureServerSocket(ConsumerFunc<ServerSocketChannel> functionToConfigureServerSocket) {
		this.functionToConfigureServerSocket = functionToConfigureServerSocket;
		return this;
	}

	public boolean isValidateRouteIdsOnStartup() {
		return validateRouteIdsOnStartup;
	}

	public WebServerConfig setValidateRouteIdsOnStartup(boolean validateRouteIdsOnStartup) {
		this.validateRouteIdsOnStartup = validateRouteIdsOnStartup;
		return this;
	}

	public Locale getDefaultLocale() {
		return defaultLocale;
	}

	public WebServerConfig setDefaultLocale(Locale defaultLocale) {
		this.defaultLocale = defaultLocale;
		return this;
	}

	public Long getStaticFileCacheTimeSeconds() {
		return staticFileCacheTimeSeconds ;
	}

	public WebServerConfig setStaticFileCacheTimeSeconds(Long staticFileCacheTimeSeconds) {
		this.staticFileCacheTimeSeconds = staticFileCacheTimeSeconds;
		return this;
	}

	public int getHttp2EngineThreadCount() {
		return http2EngineThreadCount;
	}

	public WebServerConfig setHttp2EngineThreadCount(int http2EngineThreadCount) {
		this.http2EngineThreadCount = http2EngineThreadCount;
		return this;
	}

	public Http2Config getHttp2Config() {
		return http2Config ;
	}

	public WebServerConfig setHttp2Config(Http2Config http2Config) {
		this.http2Config = http2Config;
		return this;
	}

	public BackpressureConfig getBackpressureConfig() {
		return backpressureConfig;
	}

	public WebServerConfig setBackpressureConfig(BackpressureConfig backpressureConfig) {
		this.backpressureConfig = backpressureConfig;
		return this;
	}
	
	public Charset getDefaultFormAcceptEncoding() {
		return defaultFormAcceptEncoding;
	}
	
	public WebServerConfig setDefaultFormAcceptEncoding(Charset defaultFormAcceptEncoding) {
		this.defaultFormAcceptEncoding = defaultFormAcceptEncoding;
		return this;
	}
}
