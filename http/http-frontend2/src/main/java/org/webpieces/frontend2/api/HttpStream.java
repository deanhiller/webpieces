package org.webpieces.frontend2.api;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.error.ShutdownStream;

//nearly one to one with RequestStreamHandle.java on purpose.  rename to HttpStreamHandle or FrontendStreamHandle?
public interface HttpStream {

	
	StreamRef incomingRequest(Http2Request request, ResponseStream stream);

	//void incomingCancel(ShutdownStream shutdown);
	
}
