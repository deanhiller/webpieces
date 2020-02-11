package org.webpieces.httpclientx.impl;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpparser.api.dto.HttpRequest;

public class Translations2 {

	public static HttpFullRequest translate(FullRequest request) {
		HttpRequest httpReq = new HttpRequest();
		DataWrapper data = request.getPayload();
		HttpFullRequest req = new HttpFullRequest(httpReq, data);
		return null;
	}

	public static FullResponse translate(HttpFullResponse r) {
		throw new UnsupportedOperationException("need to implement");
	}

}
