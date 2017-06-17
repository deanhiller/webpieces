package org.webpieces.throughput;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.http2client.api.Http2Client;

public abstract class ThroughputSetup {

	protected void start(boolean multiThreaded, Mode clientConfig, Mode svrConfig) throws InterruptedException {
		CompletableFuture<InetSocketAddress> future;
		if(svrConfig == Mode.ASYNCHRONOUS) {
			ServerAsync svr = new ServerAsync(multiThreaded);
			future = svr.start();
		} else {
			ServerHttp1_1Sync svr = new ServerHttp1_1Sync();
			future = svr.start();
		}
		
		future.thenApply(addr -> runClient(addr, multiThreaded, clientConfig));

		synchronized(this) {
			this.wait(); //wait forever
		}
	}
	
	protected abstract Http2Client createClient(boolean multiThreaded);
	protected abstract void runSyncClient(InetSocketAddress svrAddress);
	
	private Void runClient(InetSocketAddress svrAddress, boolean multiThreaded, Mode clientConfig) {
		if(clientConfig == Mode.ASYNCHRONOUS) {
			runAsyncClient(svrAddress, multiThreaded);
		} else {
			runSyncClient2(svrAddress);
		}
		return null;
	}

	private void runSyncClient2(InetSocketAddress svrAddress) {
		//If in single threaded mode, we cannot block the server selector thread so start a thread up
		Runnable r = new Runnable() {
			@Override
			public void run() {
				runSyncClient(svrAddress);
			}
		};
		Thread t = new Thread(r);
		t.setName("clientReadThread");
		t.start();
	}

	private void runAsyncClient(InetSocketAddress svrAddress, boolean multiThreaded) {
		Http2Client client = createClient(multiThreaded);
		ClientAsync async = new ClientAsync(client);
		async.runAsyncClient(svrAddress);
	}
}
