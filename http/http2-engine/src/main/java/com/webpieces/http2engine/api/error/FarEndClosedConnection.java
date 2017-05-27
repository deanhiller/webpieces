package com.webpieces.http2engine.api.error;

import com.webpieces.http2parser.api.dto.error.CancelReasonCode;

public class FarEndClosedConnection implements ConnReset2 {

	private String message;

	public FarEndClosedConnection(String reason) {
		this.message = reason;
	}

	@Override
	public CancelReasonCode getReasonCode() {
		return CancelReasonCode.REMOTE_CLOSED_SOCKET;
	}

	public String getReason() {
		return message;
	}

}
