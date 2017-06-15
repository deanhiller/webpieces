package org.webpieces.frontend2.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.frontend2.api.ServerSocketInfo;
import org.webpieces.frontend2.api.StreamListener;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.StreamWriter;

public class TimeoutHandler {

	private StreamListener httpListener;

	public TimeoutHandler(StreamListener httpListener) {
		this.httpListener = httpListener;
	}

	public CompletableFuture<StreamWriter> incomingRequest(ResponseStream stream, Http2Request headers, ServerSocketInfo type) {
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
