package com.webpieces.http2engine.api.error;

import com.webpieces.http2parser.api.dto.error.CancelReasonCode;

public interface ConnectionCancelled {

	String getReason();
	
	CancelReasonCode getReasonCode();

}
