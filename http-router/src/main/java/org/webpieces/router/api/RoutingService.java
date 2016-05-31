package org.webpieces.router.api;

import org.webpieces.router.api.dto.Request;

public interface RoutingService {

	void start();

	void stop();

	void processHttpRequests(Request req);
	
}
