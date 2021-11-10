package org.webpieces.asyncserver.api;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

import org.webpieces.nio.api.channels.TCPServerChannel;

public interface AsyncServer {

	public XFuture<Void> start(SocketAddress bindAddr);
	
	/**
	 * Closes the server channel and then closes all existing channels that are open
	 * @return
	 */
    public XFuture<Void> closeServerChannel();
    
	/**
	 * Puts the server in a mode where all incoming connections are sent the response in the
	 * ByteBuffer provided and then the connection is closed immediately.  This will not
	 * affect existing connections.  This is so your system itself can stay stable under high load
	 * and perform gracefully for the requests that do get through in a reasonable time.
	 * 
	 * @param overloadResponse
	 */
	public void enableOverloadMode(ByteBuffer overloadResponse);
	
	/**
	 * Start accepting connections again done after calling enableOverloadMode().  OverloadMode
	 * is not enabled to start with.
	 */
	public void disableOverloadMode();

	public TCPServerChannel getUnderlyingChannel();

}
