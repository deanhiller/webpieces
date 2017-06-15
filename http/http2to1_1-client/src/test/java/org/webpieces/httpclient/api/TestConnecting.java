package org.webpieces.httpclient.api;

import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.httpclient.api.mocks.MockChannel;
import org.webpieces.httpclient.api.mocks.MockChannelMgr;

public class TestConnecting {

	private MockChannelMgr mockChannelMgr = new MockChannelMgr();
	private MockChannel mockChannel = new MockChannel();
	private Http2Client httpClient;

	@Before
	public void setup() {
		BufferPool pool = new BufferCreationPool();
		httpClient = Http2to1_1ClientFactory.createHttpClient(mockChannelMgr, pool);
	}

	@Test
	public void testConnecting() {
		mockChannelMgr.addTCPChannelToReturn(mockChannel);
		Http2Socket socket = httpClient.createHttpSocket("clientSocket1");

		CompletableFuture<Channel> future1 = new CompletableFuture<Channel>();
		mockChannel.setConnectFuture(future1);
		CompletableFuture<Http2Socket> future = socket.connect(new InetSocketAddress(8080));
		
		Assert.assertFalse(future.isDone());
		
		future1.complete(null);
		
		Assert.assertTrue(future.isDone());
		
	}

}
