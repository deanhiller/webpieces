package com.webpieces.http2.api.streaming;

import com.webpieces.http2.api.dto.highlevel.Http2Request;

public interface RequestStreamHandle {

	StreamRef process(Http2Request request, ResponseStreamHandle responseListener);
	
}
