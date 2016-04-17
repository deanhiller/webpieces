package org.webpieces.httpclient.impl;

import org.webpieces.httpclient.api.HttpCallback;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.nio.api.ChannelManager;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.dto.HttpRequest;

public class HttpClientImpl implements HttpClient {

	private ChannelManager mgr;
	private HttpParser parser;

	public HttpClientImpl(ChannelManager mgr, HttpParser parser) {
		this.mgr = mgr;
		this.parser = parser;
	}

	@Override
	public void sendSingleRequest(HttpRequest request, HttpCallback cb) {
	}
	
	

}
