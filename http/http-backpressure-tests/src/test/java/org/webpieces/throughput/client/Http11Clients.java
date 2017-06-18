package org.webpieces.throughput.client;

import java.net.InetSocketAddress;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.httpclient.api.Http2to1_1ClientFactory;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;

public class Http11Clients implements Clients {

	private boolean multiThreaded;

	public Http11Clients(boolean multiThreaded) {
		this.multiThreaded = multiThreaded;
	}

	@Override
	public Http2Client createClient() {
		if(!multiThreaded) {
			BufferCreationPool pool = new BufferCreationPool();
			ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
			ChannelManager chanMgr = factory.createSingleThreadedChanMgr("clientCmLoop", pool, new BackpressureConfig());
			
			Http2Client client = Http2to1_1ClientFactory.createHttpClient(chanMgr, pool);
			return client;
		}
		
		return Http2to1_1ClientFactory.createHttpClient(20);
	}
	
	@Override
	public SynchronousClient createSyncClient() {
		return new Http11SynchronousClient();
	}
	
}
