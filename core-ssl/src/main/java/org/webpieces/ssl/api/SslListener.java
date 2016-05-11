package org.webpieces.ssl.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface SslListener {
	/**
	 * This is basically the SSL connected even in that your channel is now secure so go ahead and start writing to
	 * to the AsynchSSLEngine interface.
	 * 
	 * @throws IOException
	 */
	void encryptedLinkEstablished();

	/**
	 * This is in a synchronization block so must be executed as quickly as possibly while keeping the calls that
	 * come from this method in order so that the other end receives them in order or it will not work.
	 * 
	 * @param engineToSocketData
	 */
	CompletableFuture<Void> packetEncrypted(ByteBuffer engineToSocketData);

	/**
	 * This is in a synchronization block so must be executed as quickly as possibly while keeping the calls that
	 * come from this method in order so that the other end receives them in order or it will not work.
	 * 
	 * @param engineToSocketData
	 */
	void sendEncryptedHandshakeData(ByteBuffer engineToSocketData);
	
	/**
	 * This is NOT in a synchronization block in case clients take a long time processing the packet
	 * 
	 * @param out
	 */
	void packetUnencrypted(ByteBuffer out);
	
	/**
	 * Java's SSL Engine gives us a Runnable so you can run it on a different thread.  In reality, most clients
	 * are have a thread pool and just run it on the same thread so you can just call r.run() when you implement
	 * this method.  
     *
	 * @param r
	 */
	void runTask(Runnable r);

	/**
	 * Called when the engine is closed after initiating a close
	 *
	 * @param clientInitiated true if client called close or initateClose.  false
	 * if closed was caused by far end sending close handshake message.
	 */
	void closed(boolean clientInitiated);
}
