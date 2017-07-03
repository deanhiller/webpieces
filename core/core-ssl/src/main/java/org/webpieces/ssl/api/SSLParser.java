package org.webpieces.ssl.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.ssl.api.dto.SslAction;

public interface SSLParser {

	/**
	 * This is for the client to start the handshake and is not generally needed on the server side.
	 * 
	 * @return
	 */
	CompletableFuture<List<SslAction>> beginHandshake();
	
	/**
	 * Asynchronous in case the client wants to run the Runnables that SSLEngine returns offline in a different thread
	 * pool.  This is not advised in webpieces though as all the ssl is in a threadpool to begin with
	 * 
	 * @param dataWrapper
	 * @return a list of SSL actions the client needs to take like send encrypted packets to remote end AND fire link established
	 * to the client app (this is a result of sometimes a list of actions needs to come back for the sslEngine
	 */
	CompletableFuture<List<SslAction>> parseIncoming(DataWrapper dataWrapper);

}
