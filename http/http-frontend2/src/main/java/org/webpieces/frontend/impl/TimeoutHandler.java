package org.webpieces.frontend.impl;

import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.frontend.api.FrontendStream;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.frontend.api.Protocol;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;

public class TimeoutHandler {

	private HttpRequestListener httpListener;


	public TimeoutHandler(HttpRequestListener httpListener) {
		this.httpListener = httpListener;
	}

	public StreamWriter incomingRequest(FrontendStream stream, Http2Headers headers, Protocol type) {
		FrontendSocket socket = stream.getSocket();
		
		//record last incoming data timestamp
		return httpListener.incomingRequest(stream, headers, type); 
	}
	
	public void connectionOpened(FrontendSocket channel, boolean isReadyForWrites) {
		//start open connection timer
	}

	public void connectionClosedBeforeRequest(FrontendSocketImpl socket) {
	}

}
