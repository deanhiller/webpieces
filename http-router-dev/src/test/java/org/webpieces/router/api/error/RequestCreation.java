package org.webpieces.router.api.error;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.Request;

public class RequestCreation {
	public static Request createHttpRequest(HttpMethod method, String path) {
		Request r = new Request();
		r.method = method;
		r.relativePath = path;
		
		return r;
	}
}
