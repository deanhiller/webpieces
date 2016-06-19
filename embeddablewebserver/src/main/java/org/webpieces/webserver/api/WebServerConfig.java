package org.webpieces.webserver.api;

import org.webpieces.nio.api.SSLEngineFactory;

import com.google.inject.Module;

public class WebServerConfig {

	private int numFrontendServerThreads = 20;
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

	public SSLEngineFactory getSSLEngineFactory() {
		return sslEngineFactory;
	}

	public WebServerConfig setSslEngineFactory(SSLEngineFactory sslEngineFactory) {
		this.sslEngineFactory = sslEngineFactory;
		return this;
	}
	
}
