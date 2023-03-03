package org.webpieces.ssl.api;

import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

public interface SslListener {
	/**
	 * This is basically the SSL connected even in that your channel is now secure so go ahead and start writing to
	 * to the AsynchSSLEngine interface.
	 * 
	 */
	void encryptedLinkEstablished();

	/**
	 * This is in a synchronization block so must be executed as quickly as possibly while keeping the calls that
	 * come from this method in order so that the other end receives them in order or it will not work.
	 * 
	 * @param engineToSocketData
	 */
	XFuture<Void> packetEncrypted(ByteBuffer engineToSocketData);

	/**
	 * This is in a synchronization block so must be executed as quickly as possibly while keeping the calls that
	 * come from this method in order so that the other end receives them in order or it will not work.
	 * 
	 * @param engineToSocketData
	 */
	XFuture<Void> sendEncryptedHandshakeData(ByteBuffer engineToSocketData);
	
	/**
	 * This is NOT in a synchronization block in case clients take a long time processing the packet
	 * 
	 * @param out
	 */
	XFuture<Void> packetUnencrypted(ByteBuffer out);

	/**
	 * Called when the engine is closed after initiating a close
	 *
	 * @param clientInitiated true if client called close or initateClose.  false
	 * if closed was caused by far end sending close handshake message.
	 * @param exc Only filled in on failure
	 */
	void closed(boolean clientInitiated, Exception exc);
}
