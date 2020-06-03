package org.webpieces.frontend2.api;

import java.util.Map;

import com.webpieces.http2.api.streaming.ResponseStreamHandle;

public interface ResponseStream extends ResponseStreamHandle {

	FrontendSocket getSocket();
	
    Map<String, Object> getSession();

}
