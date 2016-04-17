package org.webpieces.httpclient.api;

import com.webpieces.httpparser.api.dto.HttpResponse;

public interface HttpCallback {

	/**
	 * Could be a failure response (ie. non-2xx)..
	 */
	public void response(HttpResponse response);
	
	public void exception(Throwable e);
}
