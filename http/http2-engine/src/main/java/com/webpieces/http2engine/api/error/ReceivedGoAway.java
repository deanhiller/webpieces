package com.webpieces.http2engine.api.error;

import com.webpieces.http2.api.dto.error.CancelReasonCode;
import com.webpieces.http2.api.dto.lowlevel.GoAwayFrame;

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
