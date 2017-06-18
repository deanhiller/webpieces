package org.webpieces.throughput.client;

import org.webpieces.http2client.api.Http2Client;

public interface Clients {

	Http2Client createClient();

	SynchronousClient createSyncClient();

}
