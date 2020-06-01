package org.webpieces.httpclient.mocks;

import org.webpieces.httpclient11.api.HttpResponseListener;
import org.webpieces.httpclient11.api.HttpStreamRef;
import org.webpieces.httpparser.api.dto.HttpResponse;

public class MockResponseListener implements HttpResponseListener {

	@Override
	public HttpStreamRef incomingResponse(HttpResponse resp, boolean isComplete) {
		return null;
	}

	@Override
	public void failure(Throwable e) {
	}

}
