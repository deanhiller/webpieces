package com.webpieces.http2engine.api;

import com.webpieces.http2parser.api.dto.DataFrame;

public class Http2DataPayload implements Http2Payload {

	private DataFrame dataFrame;
	
	public Http2DataPayload(DataFrame dataFrame) {
		this.dataFrame = dataFrame;
	}

	@Override
	public int getStreamId() {
		return dataFrame.getStreamId();
	}

	@Override
	public boolean isEndStream() {
		return dataFrame.isEndStream();
	}

	public DataFrame getDataFrame() {
		return dataFrame;
	}

}
