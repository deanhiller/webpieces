package org.webpieces.webserver.json;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.PushStreamHandle;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamWriter;

public class MockResponseStreamHandle implements ResponseStreamHandle {

	@Override
	public XFuture<StreamWriter> process(Http2Response response) {
		return null;
	}

	@Override
	public PushStreamHandle openPushStream() {
		return null;
	}

	@Override
	public XFuture<Void> cancel(CancelReason reason) {
		return null;
	}

}
