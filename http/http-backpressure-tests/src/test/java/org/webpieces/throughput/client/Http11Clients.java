package org.webpieces.throughput.client;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.httpclientx.api.Http2to1_1ClientFactory;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.throughput.AsyncConfig;

public class Http11Clients implements Clients {

	private AsyncConfig config;

	public Http11Clients(AsyncConfig config) {
		this.config = config;
	}

	@Override
	public Http2Client createClient() {
		if(config.getClientThreadCount() != null)
			return Http2to1_1ClientFactory.createHttpClient(config.getClientThreadCount(), config.getBackpressureConfig());
			
		//single threaded version...
		BufferCreationPool pool = new BufferCreationPool();
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager chanMgr = factory.createSingleThreadedChanMgr("clientCmLoop", pool, config.getBackpressureConfig());
		
		Http2Client client = Http2to1_1ClientFactory.createHttpClient(chanMgr, pool);
		return client;
	}
	
	@Override
	public SynchronousClient createSyncClient() {
		return new Http11SynchronousClient();
	}
	
}
