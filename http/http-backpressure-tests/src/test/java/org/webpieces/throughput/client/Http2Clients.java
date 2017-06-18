package org.webpieces.throughput.client;

import java.net.InetSocketAddress;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2ClientFactory;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;

import com.webpieces.http2engine.api.client.Http2Config;

public class Http2Clients implements Clients {

	private boolean multiThreaded;

	public Http2Clients(boolean multiThreaded) {
		this.multiThreaded = multiThreaded;
	}

	@Override
	public Http2Client createClient() {
		if(multiThreaded)
			return Http2ClientFactory.createHttpClient(20);
		
		BufferCreationPool pool = new BufferCreationPool();
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager chanMgr = factory.createSingleThreadedChanMgr("clientCmLoop", pool, new BackpressureConfig());
		
		return Http2ClientFactory.createHttpClient(new Http2Config(), chanMgr, pool);
	}

	@Override
	public SynchronousClient createSyncClient() {
		return new Http2SynchronousClient();
	}

}
