package org.webpieces.throughput.client;

import java.net.InetSocketAddress;

public interface SynchronousClient {

	void start(InetSocketAddress svrAddress);

}
