package org.webpieces.webserver.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;

public class WebBrowserSimulator {

	private HttpSocket socket;
	private Map<String, String> cookieToValue = new HashMap<>();

	public WebBrowserSimulator(HttpSocket socket) {
		this.socket = socket;
	}

	public ResponseWrapper send(HttpFullRequest request) {
		if(cookieToValue.size() > 0) {
			request.addHeader(createCookieHeader());
		}

		CompletableFuture<HttpFullResponse> respFuture = socket.send(request);

		ResponseWrapper responseWrapper = ResponseExtract.waitResponseAndWrap(respFuture);
		
		cookieToValue = responseWrapper.modifyCookieMap(cookieToValue);
		
		return responseWrapper;
	}
	
	public Header createCookieHeader() {
		boolean isFirstLine = true;
		String fullRequestCookie = "";
		for(Entry<String, String> entry : cookieToValue.entrySet()) {
			
			if(isFirstLine) {
				isFirstLine = false;
				fullRequestCookie += entry.getKey()+"="+entry.getValue();
			} else
				fullRequestCookie += "; "+entry.getKey()+"="+entry.getValue();
		}
		
		return new Header(KnownHeaderName.COOKIE, fullRequestCookie);
	}
	
}
