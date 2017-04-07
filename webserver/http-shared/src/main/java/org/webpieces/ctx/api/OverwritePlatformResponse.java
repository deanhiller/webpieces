package org.webpieces.ctx.api;

import org.webpieces.httpparser.api.dto.HttpResponse;

public interface OverwritePlatformResponse {

	public HttpResponse modifyOrReplace(HttpResponse response);
	
}
