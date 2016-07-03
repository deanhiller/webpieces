package org.webpieces.router.api.error;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.RouterRequest;

public class RequestCreation {
	public static RouterRequest createHttpRequest(HttpMethod method, String path) {
		RouterRequest r = new RouterRequest();
		r.method = method;
		r.relativePath = path;
		
		return r;
	}
}
