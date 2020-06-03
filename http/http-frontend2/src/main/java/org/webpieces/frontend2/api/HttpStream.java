package org.webpieces.frontend2.api;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.streaming.StreamRef;

//nearly one to one with RequestStreamHandle.java on purpose.  rename to HttpStreamHandle or FrontendStreamHandle?
public interface HttpStream {

	
	StreamRef incomingRequest(Http2Request request, ResponseStream stream);

}
