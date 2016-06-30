package org.webpieces.nio.api;

import javax.net.ssl.SSLEngine;

public interface SSLEngineFactoryWithHost extends SSLEngineFactory {

	public SSLEngine createSslEngine(String host);
	
}
