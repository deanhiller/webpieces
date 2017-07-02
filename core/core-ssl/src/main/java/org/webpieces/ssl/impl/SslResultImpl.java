package org.webpieces.ssl.impl;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.ssl.api.SslResult;
import org.webpieces.ssl.api.SslState;

public class SslResultImpl implements SslResult {

	private DataWrapper encryptedData;
	private DataWrapper decryptedData;
	private boolean isClientInitiatedClosed;
	private SslState state;

	public SslResultImpl(SslState state, DataWrapper encryptedData, DataWrapper decryptedData, boolean isClientInitiatedClosed) {
		this.state = state;
		this.encryptedData = encryptedData;
		this.decryptedData = decryptedData;
		this.isClientInitiatedClosed = isClientInitiatedClosed;
	}

	@Override
	public SslState getSslState() {
		return state;
	}
	
	@Override
	public DataWrapper getEncryptedData() {
		return encryptedData;
	}
	@Override
	public DataWrapper getDecryptedData() {
		return decryptedData;
	}
	@Override
	public boolean isClientInitiatedClosed() {
		return isClientInitiatedClosed;
	}

}
