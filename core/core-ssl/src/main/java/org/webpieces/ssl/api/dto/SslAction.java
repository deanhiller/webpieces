package org.webpieces.ssl.api.dto;

import org.webpieces.data.api.DataWrapper;

public class SslAction {

	private SslActionEnum sslAction;
	private DataWrapper encryptedData;
	private DataWrapper decryptedData;
	
	public SslAction(SslActionEnum sslAction, DataWrapper encryptedData, DataWrapper decryptedData) {
		super();
		this.sslAction = sslAction;
		this.encryptedData = encryptedData;
		this.decryptedData = decryptedData;
	}
	
	public SslActionEnum getSslAction() {
		return sslAction;
	}
	public DataWrapper getEncryptedData() {
		return encryptedData;
	}
	public DataWrapper getDecryptedData() {
		return decryptedData;
	}

}
