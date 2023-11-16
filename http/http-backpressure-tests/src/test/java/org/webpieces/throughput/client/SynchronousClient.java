package org.webpieces.throughput.client;

import org.webpieces.util.HostWithPort;

public interface SynchronousClient {

	void start(HostWithPort svrAddress);

}
