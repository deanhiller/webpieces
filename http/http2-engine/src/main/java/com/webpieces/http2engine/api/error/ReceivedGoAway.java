package com.webpieces.http2engine.api.error;

import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.error.CancelReasonCode;

public class ReceivedGoAway implements ConnReset2 {
	
	private String message;
	private GoAwayFrame goAwayFrame;

	public ReceivedGoAway(String reason, GoAwayFrame goAwayFrame) {
		this.message = reason;
		this.goAwayFrame = goAwayFrame;
	}

	@Override
	public CancelReasonCode getReasonCode() {
		return CancelReasonCode.RECEIVED_GO_AWAY_FROM_REMOTE_END;
	}

	public String getReason() {
		return message;
	}

	public GoAwayFrame getGoAwayFrame() {
		return goAwayFrame;
	}
	
}
