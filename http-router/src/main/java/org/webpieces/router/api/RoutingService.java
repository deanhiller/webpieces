package org.webpieces.router.api;

import org.webpieces.router.api.dto.RouterRequest;

public interface RoutingService {

	void start();

	void stop();

	void processHttpRequests(RouterRequest req, ResponseStreamer streamer);
	
}
