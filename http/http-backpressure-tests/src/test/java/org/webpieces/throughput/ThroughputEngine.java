package org.webpieces.throughput;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.Protocol;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.throughput.client.ClientAsync;
import org.webpieces.throughput.client.Clients;
import org.webpieces.throughput.client.Http11Clients;
import org.webpieces.throughput.client.Http2Clients;
import org.webpieces.throughput.client.SynchronousClient;
import org.webpieces.throughput.server.ServerAsync;
import org.webpieces.throughput.server.ServerHttp1_1Sync;
import org.webpieces.throughput.server.ServerHttp2Sync;

public class ThroughputEngine {

	protected void start(Mode clientConfig, Mode svrConfig, Protocol protocol) throws InterruptedException {
		boolean multiThreaded = false;
		CompletableFuture<InetSocketAddress> future;
		if(svrConfig == Mode.ASYNCHRONOUS) {
			//The asynchronous server supports BOTH protocols and automatically ends up doing
			//the protocol of the client...
			ServerAsync svr = new ServerAsync(multiThreaded);
			future = svr.start();
		} else if(protocol == Protocol.HTTP11){
			ServerHttp1_1Sync svr = new ServerHttp1_1Sync();
			future = svr.start();
		} else {
			ServerHttp2Sync svr = new ServerHttp2Sync();
			future = svr.start();
		}
		
		future.thenApply(addr -> runClient(addr, multiThreaded, clientConfig, protocol));

		synchronized(this) {
			this.wait(); //wait forever
		}
	}
	
	private Void runClient(InetSocketAddress svrAddress, boolean multiThreaded, Mode clientConfig, Protocol protocol) {
		Clients creator;
		if(protocol == Protocol.HTTP11)
			creator = new Http11Clients(multiThreaded);
		else
			creator = new Http2Clients(multiThreaded);
		
		if(clientConfig == Mode.ASYNCHRONOUS) {
			runAsyncClient(svrAddress, protocol, creator);
		} else {
			runSyncClient2(svrAddress, protocol, creator);
		}
		return null;
	}

	private void runSyncClient2(InetSocketAddress svrAddress, Protocol protocol, Clients creator) {
		SynchronousClient client = creator.createSyncClient();
		//If in single threaded mode, we cannot block the server selector thread so start a thread up
		Runnable r = new Runnable() {
			@Override
			public void run() {
				client.start(svrAddress);
			}
		};
		Thread t = new Thread(r);
		t.setName("clientReadThread");
		t.start();
	}

	private void runAsyncClient(InetSocketAddress svrAddress, Protocol protocol, Clients creator) {
		Http2Client client = creator.createClient();
		ClientAsync async = new ClientAsync(client, protocol);
		async.runAsyncClient(svrAddress);
	}
}
