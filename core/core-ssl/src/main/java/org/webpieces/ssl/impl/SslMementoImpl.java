package org.webpieces.ssl.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLEngine;

import org.webpieces.ssl.api.ConnectionState;

public class SslMementoImpl {

	private SSLEngine engine;
	private String id;
	private AtomicReference<ConnectionState> connectionState = new AtomicReference<ConnectionState>(ConnectionState.NOT_STARTED);
	private ByteBuffer cachedOut;
	private ByteBuffer cacheToProcess;
	
	public SslMementoImpl(String id, SSLEngine engine, ByteBuffer cachedOut) {
		this.id = id;
		this.engine = engine;
		this.cachedOut = cachedOut;
	}

	public SSLEngine getEngine() {
		return engine;
	}

	@Override
	public String toString() {
		return "[" + id + "]";
	}

	public ConnectionState getConnectionState() {
		return connectionState.get();
	}
	
	public void compareSet(ConnectionState expected, ConnectionState state) {
		this.connectionState.compareAndSet(expected, state);
	}

	public ByteBuffer getCachedOut() {
		return cachedOut;
	}

	public void setCachedOut(ByteBuffer cachedOut) {
		this.cachedOut = cachedOut;
	}

	public void setCachedEncryptedData(ByteBuffer encryptedData) {
		cacheToProcess = encryptedData;
	}
	
	public ByteBuffer getCachedToProcess() {
		return cacheToProcess;
	}

}
