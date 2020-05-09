package org.webpieces.webserver.api;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.webpieces.nio.api.BackpressureConfig;

import com.google.inject.Module;
import com.webpieces.http2engine.api.client.Http2Config;

public class WebServerConfig {

	private String id = "webpiecesA";
	//typically only true during a test, but could be done before production server runs as well though it will slow down startup time
	private boolean validateRouteIdsOnStartup = false;
	
	private int numFrontendServerThreads = 20;
	private int http2EngineThreadCount = 20;

	/**
	 * Not used in production but we use this to override certain objects for a development
	 * server that is optimized for development but would run slower in production such that users
	 * don't have to reboot the server as they change code.  Rather, they can make all the changes they want and
	 * it gets recompiled by the eclipse compiler on-demand while the server stays running.
	 * 
	 * The eclipse compiler is not on the production classpath and only on the DevelopmentServer classpath.
	 */
	private Module platformOverrides = null;
		
	private Http2Config http2Config = new Http2Config();
	private BackpressureConfig backpressureConfig = new BackpressureConfig();
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public boolean isValidateRouteIdsOnStartup() {
		return validateRouteIdsOnStartup;
	}

	public WebServerConfig setValidateRouteIdsOnStartup(boolean validateRouteIdsOnStartup) {
		this.validateRouteIdsOnStartup = validateRouteIdsOnStartup;
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

}
