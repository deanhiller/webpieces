package org.webpieces.webserver.test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;

//A proxy to the real server for testing purposes
public class Http11ClientSimulator {

	private MockChannelManager mgr;
	private HttpParser parser = HttpParserFactory.createParser(new BufferCreationPool());

	public Http11ClientSimulator(MockChannelManager mgr) {
		this.mgr = mgr;
	}

	public Http11Socket openHttp() {
		ConnectionListener listener = mgr.getHttpConnection();
		MockTcpChannel channel = new MockTcpChannel(parser);

		CompletableFuture<DataListener> connected = listener.connected(channel, true);
		try {
			DataListener dataListener = connected.get(2, TimeUnit.SECONDS);
			return new Http11Socket(dataListener, channel, parser);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public Http11Socket openHttps() {
		ConnectionListener listener = mgr.getHttpsConnection();
		MockTcpChannel channel = new MockTcpChannel(parser);

		CompletableFuture<DataListener> connected = listener.connected(channel, true);
		try {
			DataListener dataListener = connected.get(2, TimeUnit.SECONDS);
			return new Http11Socket(dataListener, channel, parser);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}		
	}
}
