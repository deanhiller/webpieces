package org.webpieces.ssl.api;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface AsyncSSLEngine {

	/**
	 * Begins a handshake on one end (or asks for a re-handhsake)
	 */
	CompletableFuture<Void> beginHandshake();

	/**
	 * Feeding an encrypted packet results in one of four possible methods on SSLListener being called
	 * <ol>
	 *   <li>packetEncrypted - this is a handshake response message
	 *   <li>packetUnencrypted - this is a message that has been unencrypted and is ready for consumption
	 *   <li>encryptionLinkEstablished - the last handshake message was received and handshake is complete.  Only called on very first handshake(not rehandshakes)
	 *   <li>No method is called as we are waiting for more data from the stream(ie. there 
	 *   was not enough for one whole SSL packet).  We cache the data for you ;)
	 * </ol>
	 * 
	 * If begin handshake has not been called(server side generally) and you are
	 * receiving an encrypted packet, this starts a handshake and then from then
	 * on, just feed encrypted packets here
	 * 
	 * @param buffer The bytes
	 */	
	CompletableFuture<Void> feedEncryptedPacket(ByteBuffer buffer);
	
	CompletableFuture<Void> feedPlainPacket(ByteBuffer buffer);

	void close();
	
	ConnectionState getConnectionState();
}
