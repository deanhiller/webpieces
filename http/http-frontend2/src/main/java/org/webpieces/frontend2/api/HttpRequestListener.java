package org.webpieces.frontend2.api;

import com.webpieces.http2engine.api.StreamHandle;

public interface HttpRequestListener {

	StreamHandle openStream(FrontendStream stream, SocketInfo info);
	
}
