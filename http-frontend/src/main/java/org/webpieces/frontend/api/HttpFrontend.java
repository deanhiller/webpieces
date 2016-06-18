package org.webpieces.frontend.api;

import java.nio.ByteBuffer;

public interface HttpFrontend {

	void close();

	void enableOverloadMode(ByteBuffer overloadResponse);

	void disableOverloadMode();
}
