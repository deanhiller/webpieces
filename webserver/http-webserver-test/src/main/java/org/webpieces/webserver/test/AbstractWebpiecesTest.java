package org.webpieces.webserver.test;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Assert;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLEngine;

import org.webpieces.httpclient11.api.HttpClient;
import org.webpieces.httpclient11.api.HttpClientFactory;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpclient11.api.HttpSocketListener;
import org.webpieces.mock.time.MockTime;
import org.webpieces.mock.time.MockTimer;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.util.exceptions.SneakyThrow;
import org.webpieces.webserver.test.http11.DirectHttp11Client;

import com.google.inject.Module;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

public class AbstractWebpiecesTest {

	protected MockChannelManager mgr = new MockChannelManager();
	protected MockTime time = new MockTime(true);
	protected MockTimer mockTimer = new MockTimer();

//	public HttpSocket connectHttpLocal() {
//		try {
//			return connectHttp(false, null);
//		} catch (InterruptedException | ExecutionException | TimeoutException e) {
//			throw SneakyThrow.sneak(e);
//		}
//	}
//
//	public HttpSocket connectHttpsLocal() {
//		try {
//			return connectHttps(false, null, null);
//		} catch (InterruptedException | ExecutionException | TimeoutException e) {
//			throw SneakyThrow.sneak(e);
//		}
//	}

	/**
	 * @deprecated Use connectHttp with no isRemote parameter AND override isRemote() IF you need
	 */
	@Deprecated
	public HttpSocket connectHttp(boolean isRemote, InetSocketAddress addr) throws InterruptedException, ExecutionException, TimeoutException {
		NullHttp1CloseListener listener = new NullHttp1CloseListener();
		HttpSocket socket = getClient(isRemote).createHttpSocket(listener);
		XFuture<Void> connect = socket.connect(addr);
		connect.get(2, TimeUnit.SECONDS);
		return socket;
	}

	public HttpSocket connectHttp(InetSocketAddress addr) {
		return connectHttp(addr, null);
	}
	
	public HttpSocket connectHttp(InetSocketAddress addr, HttpSocketListener listener) {
		if(listener == null)
			listener = new NullHttp1CloseListener();
		HttpSocket socket = getClient().createHttpSocket(listener);
		XFuture<Void> connect = socket.connect(addr);
		try {
			connect.get(2, TimeUnit.SECONDS);

			return socket;
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	/**
	 * @deprecated Use connectHttp with no isRemote parameter AND override isRemote() IF you need
	 */
	@Deprecated
	public HttpSocket connectHttps(boolean isRemote, SSLEngine engine, InetSocketAddress addr) throws InterruptedException, ExecutionException, TimeoutException {
		NullHttp1CloseListener listener = new NullHttp1CloseListener();
		HttpSocket socket = getClient(isRemote).createHttpsSocket(engine, listener);
		XFuture<Void> connect = socket.connect(addr);
		connect.get(2, TimeUnit.SECONDS);
		return socket;
	}

	public HttpSocket connectHttps(SSLEngine engine, InetSocketAddress addr) {
		return connectHttps(engine, addr, null);
	}
	
	public HttpSocket connectHttps(SSLEngine engine, InetSocketAddress addr, HttpSocketListener listener) {
		if(listener == null)
			listener = new NullHttp1CloseListener();
		
		HttpSocket socket = getClient().createHttpsSocket(engine, listener);
		XFuture<Void> connect = socket.connect(addr);
		try {
			connect.get(2, TimeUnit.SECONDS);
			return socket;
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw SneakyThrow.sneak(e);
		}
	}
	/**
	 * @deprecated Use getOverrides(MeterRegistry) instead now AND override isRemote if you like (so you can create template tests too)
	 */
	@Deprecated
	protected Module getOverrides(boolean isFullServer, MeterRegistry metrics) {
		if(isFullServer)
			return new OverridesForTestRealServer(metrics);
		return new OverridesForEmbeddedSvrWithParsing(mgr, time, mockTimer, metrics);
	}

	protected Module getOverrides(MeterRegistry metrics) {
		if(isRemote())
			return new OverridesForTestRealServer(metrics);
		return new OverridesForEmbeddedSvrWithParsing(mgr, time, mockTimer, metrics);
	}
	
	/**
	 * @deprecated Use getClient() instead and override isRemote() instead of this method 
	 */
	@Deprecated
	protected HttpClient getClient(boolean isRemote) {
		if(isRemote) {
			HttpClient client = HttpClientFactory.createHttpClient("testClient", 5, new BackpressureConfig(), Metrics.globalRegistry);
			return client;
		}
		
		/*
		 * The Client that wires itself on top of the server directly such that a developer can step through the
		 * whole webpieces server into their application to get a full picture of what is going on.  (we try to
		 * keep as few layers as possible between client and webapplication code
		 */
		return new DirectHttp11Client(mgr);
	}

	protected HttpClient getClient() {
		//IF metrics is supplied, we create the http client
		if(isRemote()) {
			HttpClient client = HttpClientFactory.createHttpClient("testClient", 5, new BackpressureConfig(), Metrics.globalRegistry);
			return client;
		}
		
		/*
		 * The Client that wires itself on top of the server directly such that a developer can step through the
		 * whole webpieces server into their application to get a full picture of what is going on.  (we try to
		 * keep as few layers as possible between client and webapplication code
		 */
		return new DirectHttp11Client(mgr);
	}
	
	protected boolean isRemote() {
		return false;
	}
	
}
