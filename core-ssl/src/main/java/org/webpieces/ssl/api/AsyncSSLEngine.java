package org.webpieces.ssl.api;

import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;

public interface AsyncSSLEngine {

	SslMemento createMemento(String loggingId, SSLEngine engine);
	
	/**
	 * Begins a handshake on one end (or asks for a re-handhsake)
	 * @param memento
	 * @return
	 */
	SslMemento beginHandshake(SslMemento memento);

	/**
	 * If begin handshake has not been called(server side generally) and you are
	 * receiving an encrypted packet, this starts a handshake and then from then
	 * on, just feed encrypted packets here
	 * 
	 * @param memento
	 * @param buffer
	 * @return
	 */
	SslMemento feedEncryptedPacket(SslMemento memento, ByteBuffer buffer);

	/**
	 * To be called after running the Runnable from Action.RUN_RUNNABLE
	 * 
	 * @return
	 */
	SslMemento runnableComplete(SslMemento memento);
	
	SslMemento feedPlainPacket(SslMemento memento, ByteBuffer buffer);

	SslMemento close(SslMemento svrMemento);
	
}
