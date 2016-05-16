package org.webpieces.httpproxy.api;

import java.nio.ByteBuffer;

public interface HttpFrontend {

	void close();

	void enableOverloadMode(ByteBuffer overloadResponse);

	void disableOverloadMode();
}
