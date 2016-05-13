package org.webpieces.asyncserver.api;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface AsyncServer {

	/**
	 * Puts the server in a mode where all incoming connections are sent the response in the
	 * ByteBuffer provided and then the connection is closed immediately.  This will not
	 * affect existing connections.  This is so you your system itself can stay stable.
	 * 
	 * @param overloadResponse
	 */
	public void enableOverloadMode(ByteBuffer overloadResponse);
	
	/**
	 * Start accepting connections again done after calling enableOverloadMode().  OverloadMode
	 * is not enabled to start with.
	 */
	public void disableOverloadMode();
	
	/**
	 * Closes the server channel and then closes all existing channels that are open
	 * @return
	 */
    public CompletableFuture<Void> closeServerChannel();

	public InetSocketAddress getBoundAddr();
    
}
