package org.webpieces.asyncserver.api;

import java.util.List;

import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.handlers.AsyncDataListener;

public interface AsyncServerManager {

	AsyncServer createTcpServer(AsyncConfig config, AsyncDataListener listener);

	AsyncServer createTcpServer(AsyncConfig config, AsyncDataListener listener, SSLEngineFactory sslFactory);

	AsyncServer createTcpServer(AsyncConfig config, AsyncDataListener listener, SSLEngineFactory sslFactory, List<String> supportedAlpnProtocols);

}
