package org.webpieces.throughput.client;

import org.webpieces.data.api.TwoPools;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2ClientConfig;
import org.webpieces.http2client.api.Http2ClientFactory;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.throughput.AsyncConfig;

import com.webpieces.http2engine.api.client.Http2Config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class Http2Clients implements Clients {

	private AsyncConfig config;
	private Http2Config http2Config;
	private MeterRegistry metrics;

	public Http2Clients(AsyncConfig config, MeterRegistry metrics) {
		this.config = config;
		this.metrics = metrics;
		http2Config = new Http2Config();
		http2Config.setInitialRemoteMaxConcurrent(config.getClientMaxConcurrentRequests());
	}

	@Override
	public Http2Client createClient() {
		if(config.getClientThreadCount() != null)
			return createMultiThreadedClient();
		
		//single threaded version...
		TwoPools pool = new TwoPools("pl", new SimpleMeterRegistry());
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(metrics);
		ChannelManager chanMgr = factory.createSingleThreadedChanMgr("clientCmLoop", pool, config.getBackpressureConfig());
		return Http2ClientFactory.createHttpClient(http2Config, chanMgr, pool);
	}

	private Http2Client createMultiThreadedClient() {
		Http2ClientConfig clientConfig = new Http2ClientConfig();
		clientConfig.setBackpressureConfig(config.getBackpressureConfig());
		clientConfig.setHttp2Config(http2Config);
		clientConfig.setNumThreads(config.getClientThreadCount());

		return Http2ClientFactory.createHttpClient(clientConfig, metrics);
	}

	@Override
	public SynchronousClient createSyncClient() {
		return new Http2SynchronousClient();
	}

}
