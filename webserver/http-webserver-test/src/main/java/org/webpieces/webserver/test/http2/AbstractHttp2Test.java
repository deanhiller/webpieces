package org.webpieces.webserver.test.http2;

import java.net.InetSocketAddress;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLEngine;

import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketListener;
import org.webpieces.httpclientx.api.Http2to11ClientFactory;
import org.webpieces.mock.time.MockTime;
import org.webpieces.mock.time.MockTimer;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.util.exceptions.SneakyThrow;
import org.webpieces.webserver.test.MockChannelManager;
import org.webpieces.webserver.test.OverridesForEmbeddedSvrWithParsing;
import org.webpieces.webserver.test.OverridesForTestRealServer;
import org.webpieces.webserver.test.http2.direct.DirectHttp2Client;
import org.webpieces.webserver.test.http2.directfast.DirectFastClient;
import org.webpieces.webserver.test.http2.directfast.MockFrontendManager;
import org.webpieces.webserver.test.http2.directfast.OverridesForEmbeddedSvrNoParsing;

import com.google.inject.Module;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

public abstract class AbstractHttp2Test {

	protected MockChannelManager mgr = new MockChannelManager();
	protected MockFrontendManager frontEnd = new MockFrontendManager();
	protected MockTime time = new MockTime(true);
	protected MockTimer mockTimer = new MockTimer();

	public Http2Socket connectHttp(InetSocketAddress addr) {
		return connectHttp(addr, null);
	}
	
	public Http2Socket connectHttp(InetSocketAddress addr, Http2SocketListener listener) {
		try {
			if(listener == null)
				listener = new NullCloseListener();
			Http2Socket socket = getClient().createHttpSocket(listener);
			XFuture<Void> connect = socket.connect(addr);
			connect.get(2, TimeUnit.SECONDS);
			return socket;
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	public Http2Socket connectHttps(SSLEngine engine, InetSocketAddress addr) {
		return connectHttps(engine, addr, null);
	}
	
	public Http2Socket connectHttps(SSLEngine engine, InetSocketAddress addr, Http2SocketListener listener) {
		try {
			if(listener == null)
				listener = new NullCloseListener();
			Http2Socket socket = getClient().createHttpsSocket(engine, listener);
			XFuture<Void> connect = socket.connect(addr);
			connect.get(2, TimeUnit.SECONDS);
			return socket;
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	protected Module getOverrides(MeterRegistry metrics) {
		if(getTestMode() == TestMode.REMOTE) //need full server
			return new OverridesForTestRealServer(metrics);
		else if(getTestMode() == TestMode.EMBEDDED_DIRET_NO_PARSING)
			return new OverridesForEmbeddedSvrNoParsing(frontEnd, time, mockTimer, metrics);
		else //slower with parsing BUT closer to what platform does in production with no need to bind sockets
			return new OverridesForEmbeddedSvrWithParsing(mgr, time, mockTimer, metrics);
	}

	protected Http2Client getClient() {
		if(getTestMode() == TestMode.REMOTE) {
			return createRemoteClient();
		} else if(getTestMode() == TestMode.EMBEDDED_DIRET_NO_PARSING) {
			/*
			 * The Client that wires itself on top of the server directly such that a developer can step through the
			 * whole webpieces server into their application to get a full picture of what is going on.  (we try to
			 * keep as few layers as possible between client and webapplication code
			 */
			return new DirectFastClient(frontEnd);
		} else {
			//does http2 hpack, http2 parsing, http2 engine work, etc. so you can see the full thing in action!!!! GREAT for understanding how it all works!!
			return new DirectHttp2Client(mgr); 
		}
	}
	
	/**
	 * Override to switch to real http2 client if you like vs. http1.1 client that exposes http2 interface
	 */
	protected Http2Client createRemoteClient() {
		return Http2to11ClientFactory.createHttpClient("testClient", 5, new BackpressureConfig(), Metrics.globalRegistry);		
	}
	
	protected TestMode getTestMode() {
		return TestMode.EMBEDDED_DIRET_NO_PARSING;
	}

}
