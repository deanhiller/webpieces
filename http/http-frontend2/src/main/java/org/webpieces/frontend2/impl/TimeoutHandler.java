package org.webpieces.frontend2.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.SocketInfo;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.StreamWriter;

public class TimeoutHandler {

	private HttpRequestListener httpListener;

	public TimeoutHandler(HttpRequestListener httpListener) {
		this.httpListener = httpListener;
	}

	public CompletableFuture<StreamWriter> incomingRequest(FrontendStream stream, Http2Request headers, SocketInfo type) {
		FrontendSocket socket = stream.getSocket();
		
		//record last incoming data timestamp
		//return httpListener.openStream(stream, headers, type); 
		return null;
	}

	public void connectionOpened(FrontendSocket channel, boolean isReadyForWrites) {
		//start open connection timer
	}

	public void connectionClosedBeforeRequest(FrontendSocketImpl socket) {
	}

}
