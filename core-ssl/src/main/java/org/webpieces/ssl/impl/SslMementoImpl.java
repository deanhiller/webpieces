package org.webpieces.ssl.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLEngine;

import org.webpieces.ssl.api.Action;
import org.webpieces.ssl.api.ConnectionState;
import org.webpieces.ssl.api.SslMemento;

public class SslMementoImpl implements SslMemento {

	private SSLEngine engine;
	private String id;
	private Action actionToTake;
	private ConnectionState connectionState = ConnectionState.NOT_CONNECTED;
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

	public void clear() {
		actionToTake = null;
	}

	@Override
	public Action getActionToTake() {
		return actionToTake;
	}

	@Override
	public ConnectionState getConnectionState() {
		return connectionState;
	}
	
	public void setConnectionState(ConnectionState state) {
		this.connectionState = state;
	}

	public void setActionToTake(Action action) {
		this.actionToTake = action;
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
