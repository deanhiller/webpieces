package org.webpieces.throughput;

import java.net.InetSocketAddress;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.httpclient.api.Http2to1_1ClientFactory;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;

public class SingleSocketHttp1_1Throughput extends ThroughputSetup {

	public static void main(String[] args) throws InterruptedException {
		SingleSocketHttp1_1Throughput example = new SingleSocketHttp1_1Throughput();
		example.start(false, Mode.SYNCHRONOUS, Mode.ASYNCHRONOUS);
	}

	@Override
	protected Http2Client createClient(boolean multiThreaded) {
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
	protected void runSyncClient(InetSocketAddress svrAddress) {
		ClientHttp1_1Sync client = new ClientHttp1_1Sync(svrAddress);
		client.start();
	}

}
