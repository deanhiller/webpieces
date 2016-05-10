package org.webpieces.ssl.api;

import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;

public interface AsyncSSLEngine {

	SslMemento createMemento(String loggingId, SSLEngine engine);
	
	SslMemento beginHandshake(SslMemento memento);

	SslMemento feedEncryptedPacket(SslMemento memento, ByteBuffer buffer);

	/**
	 * To be called after running the Runnable from Action.RUN_RUNNABLE
	 * 
	 * @return
	 */
	SslMemento runnableComplete(SslMemento memento);
	
	SslMemento feedPlainPacket(SslMemento memento, ByteBuffer buffer);
}
