package org.webpieces.webserver.test.http11;

import javax.net.ssl.SSLEngine;

import org.webpieces.data.api.TwoPools;
import org.webpieces.httpclient11.api.HttpClient;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpclient11.api.HttpSocketListener;
import org.webpieces.httpclient11.impl.HttpSocketImpl;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.digitalforge.sneakythrow.SneakyThrow;
import org.webpieces.webserver.test.MockChannelManager;
import org.webpieces.webserver.test.MockTcpChannel;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * An Http1.1 Client that sits directly on top of the webserver such that you can step into the webserver
 * from the test case to understand the full stack including your application and the platform
 * 
 * @author dhiller
 *
 */
public class DirectHttp11Client implements HttpClient {

	private MockChannelManager mgr;
	private HttpParser parser = HttpParserFactory.createParser("a", new SimpleMeterRegistry(), new TwoPools("pl", new SimpleMeterRegistry()));

	public DirectHttp11Client(MockChannelManager mgr) {
		this.mgr = mgr;
	}

	public HttpSocket createHttpSocket(HttpSocketListener closeListener) {
		ConnectionListener listener = mgr.getHttpConnection();
		MockTcpChannel channel = new MockTcpChannel(false);

		return new HttpSocketImpl(new DelayedProxy(listener, channel), parser, closeListener, false);
		//return new Http11SocketImpl(listener, channel, parser, false);

	}

	public HttpSocket createHttpsSocket(SSLEngine engine, HttpSocketListener closeListener) {
		ConnectionListener listener = mgr.getHttpsConnection();
		MockTcpChannel channel = new MockTcpChannel(true);

		return new HttpSocketImpl(new DelayedProxy(listener, channel), parser, closeListener, true);

	}
}
