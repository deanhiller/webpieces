package org.webpieces.ssl.api;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface AsyncSSLEngine {

	/**
	 * Begins a handshake on one end (or asks for a re-handhsake)
	 * @param memento
	 * @return
	 */
	void beginHandshake();

	/**
	 * Feeding an encrypted packet results in one of four possible methods on SSLListener being called
	 * <ol>
	 *   <li>packetEncrypted - this is a handshake response message
	 *   <li>packetUnencrypted - this is a message that has been unencrypted and is ready for consumption
	 *   <li>encryptionLinkEstablished - the last handshake message was received and handshake is complete
	 *   <li>No method is called as we are waiting for more data from the stream(ie. there 
	 *   was not enough for one whole SSL packet).  We cache the data for you ;)
	 * </ol>
	 *
	 * Like SSLEngine.unwrap, this method is not thread safe.  This can ONlY be called in-order and 
	 * NEVER can be called from two threads at the same time.  This ensures we do 
	 * not need a synchronization block around unwrap()/listener.packetUnencrypted which we really do not want
	 * since listener.packetUnencrypted could take a long time since that is the
	 * client code who processes the requests or responses
	 * 
	 * If begin handshake has not been called(server side generally) and you are
	 * receiving an encrypted packet, this starts a handshake and then from then
	 * on, just feed encrypted packets here
	 * 
	 * @param b The bytes
	 */	
	void feedEncryptedPacket(ByteBuffer buffer);

	/**
	 * To be called after running the Runnable from Action.RUN_RUNNABLE
	 * 
	 * @return
	 */
	//SslMemento runnableComplete(SslMemento memento);
	
	CompletableFuture<Void> feedPlainPacket(ByteBuffer buffer);

	void close();
	
	ConnectionState getConnectionState();
}
