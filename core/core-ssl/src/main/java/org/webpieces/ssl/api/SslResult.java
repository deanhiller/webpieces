package org.webpieces.ssl.api;

import org.webpieces.data.api.DataWrapper;

public interface SslResult {

	SslState getSslState();
	
	DataWrapper getEncryptedData();

	DataWrapper getDecryptedData();

	boolean isClientInitiatedClosed();

}
