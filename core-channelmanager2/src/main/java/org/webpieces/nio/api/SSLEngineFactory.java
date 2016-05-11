package org.webpieces.nio.api;

import javax.net.ssl.SSLEngine;

public interface SSLEngineFactory {

	public SSLEngine createEngineForServerSocket();
		
}
