package org.playorm.nio.api.libs;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLEngine;

public interface SSLEngineFactory {

	/**
	 * Called when a new client socket connects to a server socket and
	 * a new SSLEngine is needed to secure the connection from that client
	 * to this new socket.
	 */
	public SSLEngine createEngineForServerSocket() throws GeneralSecurityException, IOException ;
	
	/**
	 * Called when a client socket is created through the ChannelManager
	 */
	public SSLEngine createEngineForSocket() throws GeneralSecurityException, IOException ;
	
}
