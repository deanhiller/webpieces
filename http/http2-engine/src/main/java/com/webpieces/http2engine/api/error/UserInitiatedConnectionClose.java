package com.webpieces.http2engine.api.error;

import com.webpieces.http2parser.api.dto.error.CancelReasonCode;

public class UserInitiatedConnectionClose implements ShutdownConnection {

	private String reason;
	private static final CancelReasonCode failReason = CancelReasonCode.APPLICATION_INITIATED_CLOSE;

	public UserInitiatedConnectionClose(String reason) {
		super();
		this.reason = reason;
	}

	@Override
	public String getReason() {
		return reason;
	}

	@Override
	public CancelReasonCode getReasonCode() {
		return failReason;
	}
	
	
}
