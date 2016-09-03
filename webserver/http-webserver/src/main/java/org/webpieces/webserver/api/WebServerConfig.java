package org.webpieces.webserver.api;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.handlers.ConsumerFunc;

import com.google.inject.Module;

public class WebServerConfig {

	private InetSocketAddress httpListenAddress;
	private InetSocketAddress httpsListenAddress;
	//typically only true during a test, but could be done before production server runs as well though it will slow down startup time
	private boolean validateRouteIdsOnStartup = false;
	
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
	
	private ConsumerFunc<ServerSocketChannel> functionToConfigureServerSocket;
	private Charset defaultFormAcceptEncoding = StandardCharsets.UTF_8;
	private Locale defaultLocale = Locale.getDefault();
	//The max size of body for dynamic pages for Full responses and chunked responses.  This
	//is used to determine send chunks instead of full response as well since it won't fit
	//in full response sometimes
	private int maxBodySize = BufferCreationPool.DEFAULT_MAX_BUFFER_SIZE;
	
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

	public Charset getDefaultFormAcceptEncoding() {
		return defaultFormAcceptEncoding;
	}
	
	public WebServerConfig setDefaultFormAcceptEncoding(Charset defaultFormAcceptEncoding) {
		this.defaultFormAcceptEncoding = defaultFormAcceptEncoding;
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

	public int getMaxBodySize() {
		return maxBodySize;
	}

	public WebServerConfig setMaxBodySize(int fullResponseMaxSize) {
		this.maxBodySize = fullResponseMaxSize;
		return this;
	}

}
