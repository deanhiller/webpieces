package org.webpieces.throughput;

import java.net.InetSocketAddress;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2ClientFactory;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;

import com.webpieces.http2engine.api.client.Http2Config;

public class SingleSocketHttp2Throughput extends ThroughputSetup {

	public static void main(String[] args) throws InterruptedException {
		SingleSocketHttp1_1Throughput example = new SingleSocketHttp1_1Throughput();
		example.start(false, Mode.SYNCHRONOUS, Mode.ASYNCHRONOUS);
	}

	@Override
	protected Http2Client createClient(boolean multiThreaded) {
		if(multiThreaded)
			return Http2ClientFactory.createHttpClient(20);
		
		BufferCreationPool pool = new BufferCreationPool();
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager chanMgr = factory.createSingleThreadedChanMgr("clientCmLoop", pool, new BackpressureConfig());
		
		return Http2ClientFactory.createHttpClient(new Http2Config(), chanMgr, pool);
	}

	@Override
	protected void runSyncClient(InetSocketAddress svrAddress) {
	}
	
}
