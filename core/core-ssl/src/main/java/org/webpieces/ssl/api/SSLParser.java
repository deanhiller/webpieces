package org.webpieces.ssl.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;

public interface SSLParser {

	/**
	 * Asynchronous in case the client wants to run the Runnables that SSLEngine returns offline in a different thread
	 * pool.  This is not advised in webpieces though as all the ssl is in a threadpool to begin with
	 * 
	 * @param dataWrapper
	 * @return
	 */
	CompletableFuture<SslResult> parseIncoming(DataWrapper dataWrapper);

}
