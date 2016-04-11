package org.webpieces.nio.api.channels;

import java.io.IOException;
import java.net.SocketAddress;

import javax.net.ssl.SSLEngine;

import org.webpieces.nio.api.deprecated.ConnectionCallback;
import org.webpieces.nio.api.handlers.FutureOperation;

/**
 * @author Dean Hiller
 */
public interface TCPChannel extends Channel {

    /**
     * This is an asychronous connect so your thread can go do other stuff while the connection is being
     * established.  The whole SSL handshake will even happen in an asychronous fashion so your threads
     * can do work while waiting for network operations.
     * 
     * @param remoteAddr The remote address to connect to.
     * @param cb The callback implementation that will be told when the Channel is finally connected
     * @throws IOException
     * @throws InterruptedException
     */
	public void oldConnect(SocketAddress remoteAddr, ConnectionCallback cb);
	
	public boolean getKeepAlive();
	public void setKeepAlive(boolean b);
	
	/**
	 * Can switch between a socket having SSL and not having SSL by opening/closing on the socket
	 * @param engine
	 * @return
	 */
	@Deprecated
	public FutureOperation openSSL(SSLEngine engine);
	
	/**
	 * Going away....simplifying the stack.  This library won't do SSL but will just do raw tcp to 
	 * keep it simple
	 */
	@Deprecated
	public FutureOperation closeSSL();

	/**
	 * Going away....simplifying the stack.  This library won't do SSL but will just do raw tcp to 
	 * keep it simple
	 */
	@Deprecated
	public boolean isInSslMode();
}
