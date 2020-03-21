package org.webpieces.throughput.client;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.httpclientx.api.Http2to1_1ClientFactory;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.throughput.AsyncConfig;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class Http11Clients implements Clients {

	private AsyncConfig config;
	private MeterRegistry metrics;

	public Http11Clients(AsyncConfig config, MeterRegistry metrics) {
		this.config = config;
		this.metrics = metrics;
	}

	@Override
	public Http2Client createClient() {
		if(config.getClientThreadCount() != null)
			return Http2to1_1ClientFactory.createHttpClient("onlyClient", config.getClientThreadCount(), config.getBackpressureConfig(), metrics);
			
		//single threaded version...
		BufferCreationPool pool = new BufferCreationPool();
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(metrics);
		ChannelManager chanMgr = factory.createSingleThreadedChanMgr("clientCmLoop", pool, config.getBackpressureConfig());
		
		Http2Client client = Http2to1_1ClientFactory.createHttpClient("onlyClient", chanMgr, new SimpleMeterRegistry(), pool);
		return client;
	}
	
	@Override
	public SynchronousClient createSyncClient() {
		return new Http11SynchronousClient();
	}
	
}
