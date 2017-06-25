package org.webpieces.webserver.test;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLEngine;

import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.mock.time.MockTime;
import org.webpieces.mock.time.MockTimer;
import org.webpieces.webserver.test.http11.DirectHttp11Client;

public class AbstractWebpiecesTest {

	protected MockChannelManager mgr = new MockChannelManager();
	protected MockTime time = new MockTime(true);
	protected MockTimer mockTimer = new MockTimer();
	protected PlatformOverridesForTest platformOverrides = new PlatformOverridesForTest(mgr, time, mockTimer);

	/**
	 * The Client that wires itself on top of the server directly such that a developer can step through the
	 * whole webpieces server into their application to get a full picture of what is going on.  (we try to
	 * keep as few layers as possible between client and webapplication code
	 */
	protected DirectHttp11Client client = new DirectHttp11Client(mgr);

	public HttpSocket createHttpSocket(InetSocketAddress addr) throws InterruptedException, ExecutionException, TimeoutException {
		HttpSocket socket = client.createHttpSocket();
		CompletableFuture<Void> connect = socket.connect(addr);
		connect.get(2, TimeUnit.SECONDS);
		return socket;
	}

	public HttpSocket createHttpsSocket(SSLEngine engine, InetSocketAddress addr) throws InterruptedException, ExecutionException, TimeoutException {
		HttpSocket socket = client.createHttpsSocket(engine);
		CompletableFuture<Void> connect = socket.connect(addr);
		connect.get(2, TimeUnit.SECONDS);
		return socket;
	}
	
}
