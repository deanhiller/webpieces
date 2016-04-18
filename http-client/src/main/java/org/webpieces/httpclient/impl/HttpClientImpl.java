package org.webpieces.httpclient.impl;

import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.util.futures.Future;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpResponse;

public class HttpClientImpl implements HttpClient {

	private ChannelManager mgr;
	private HttpParser parser;

	public HttpClientImpl(ChannelManager mgr, HttpParser parser) {
		this.mgr = mgr;
		this.parser = parser;
	}

	@Override
	public Future<HttpResponse, Throwable> sendSingleRequest(HttpRequest request) {
		return null;
	}
	

}
