package org.webpieces.http2client.api;

import javax.net.ssl.SSLEngine;

public interface HttpsSslEngineFactory {

	public SSLEngine createSslEngine(String host, int port);
}
