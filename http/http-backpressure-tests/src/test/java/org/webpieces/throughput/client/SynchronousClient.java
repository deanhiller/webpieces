package org.webpieces.throughput.client;

import org.webpieces.nio.api.channels.HostWithPort;

import java.net.InetSocketAddress;

public interface SynchronousClient {

	void start(HostWithPort svrAddress);

}
