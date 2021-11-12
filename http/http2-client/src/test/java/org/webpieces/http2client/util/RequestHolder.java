package org.webpieces.http2client.util;

import org.webpieces.util.futures.XFuture;

import org.webpieces.http2client.mock.MockResponseListener;
import org.webpieces.http2client.mock.MockStreamWriter;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.streaming.StreamWriter;

public class RequestHolder {

	private Http2Request request;
	private MockResponseListener listener;
	private XFuture<StreamWriter> future;
	private MockStreamWriter writer;

	public RequestHolder(Http2Request request, MockResponseListener listener,
			MockStreamWriter writer, XFuture<StreamWriter> future) {
				this.request = request;
				this.listener = listener;
				this.writer = writer;
				this.future = future;
	}

	public Http2Request getRequest() {
		return request;
	}

	public MockResponseListener getListener() {
		return listener;
	}

	public XFuture<StreamWriter> getFuture() {
		return future;
	}

	public MockStreamWriter getWriter() {
		return writer;
	}
	
}
