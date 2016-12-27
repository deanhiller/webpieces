package com.webpieces.http2parser2.impl.stateful;

import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.highlevel.Http2Payload;

public class Stream {

	public void addFrame(Http2Frame f) {
	}

	public boolean isClosed() {
		return false;
	}

	public Http2Payload getPayloads() {
		return null;
	}

}
