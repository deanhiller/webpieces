package org.webpieces.ssl.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLEngine;

import org.webpieces.ssl.api.ConnectionState;

public class SslMementoImpl {

	private SSLEngine engine;
	private String id;
	private AtomicReference<ConnectionState> connectionState = new AtomicReference<ConnectionState>(ConnectionState.NOT_STARTED);
	private ByteBuffer cachedOut;
	private List<ByteBuffer> cacheToProcess = new ArrayList<>();
	private ByteBuffer cachedForUnderflow;
	
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

	public void addCachedEncryptedData(ByteBuffer encryptedData) {
		this.cacheToProcess.add(encryptedData);
	}
	
	public List<ByteBuffer> getCachedToProcess() {
		return cacheToProcess;
	}

	public void setCachedForUnderFlow(ByteBuffer encryptedData) {
		cachedForUnderflow = encryptedData;
	}
	
	public ByteBuffer getCachedForUnderFlow() {
		return cachedForUnderflow;
	}
	
}
