package org.webpieces.frontend2.impl;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.Protocol;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.server.ServerStreamWriter;

public class TimeoutHandler {

	private HttpRequestListener httpListener;


	public TimeoutHandler(HttpRequestListener httpListener) {
		this.httpListener = httpListener;
	}

	public ServerStreamWriter incomingRequest(FrontendStream stream, Http2Headers headers, Protocol type) {
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
