package com.webpieces.webserver.impl;

import org.webpieces.httpproxy.api.FrontendSocket;
import org.webpieces.httpproxy.api.HttpRequestListener;

import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.KnownStatusCode;

public class RequestReceiver implements HttpRequestListener {

	@Override
	public void processHttpRequests(FrontendSocket channel, HttpRequest req) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendServerResponse(FrontendSocket channel, Throwable exc, KnownStatusCode status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clientClosedChannel(FrontendSocket channel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void applyWriteBackPressure(FrontendSocket channel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void releaseBackPressure(FrontendSocket channel) {
		// TODO Auto-generated method stub
		
	}

}
