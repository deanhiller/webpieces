package org.webpieces.frontend2.api;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.StreamRef;

//nearly one to one with RequestStreamHandle.java on purpose.  rename to HttpStreamHandle or FrontendStreamHandle?
public interface HttpStream {

	
	StreamRef incomingRequest(Http2Request request, ResponseStream stream);

}
