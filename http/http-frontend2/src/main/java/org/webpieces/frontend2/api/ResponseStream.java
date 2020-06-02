package org.webpieces.frontend2.api;

import java.util.Map;

import com.webpieces.http2engine.api.ResponseStreamHandle;

public interface ResponseStream extends ResponseStreamHandle {

	FrontendSocket getSocket();
	
    Map<String, Object> getSession();

}
