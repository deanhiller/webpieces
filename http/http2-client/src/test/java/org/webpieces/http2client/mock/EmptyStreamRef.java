package org.webpieces.http2client.mock;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class EmptyStreamRef implements StreamRef {

	private XFuture<StreamWriter> future;

	public EmptyStreamRef(XFuture<StreamWriter> future) {
		this.future = future;
	}

	@Override
	public XFuture<StreamWriter> getWriter() {
		return future;
	}

	@Override
	public XFuture<Void> cancel(CancelReason reason) {
		throw new UnsupportedOperationException("use different mock, this one is not for testing cancel");
	}

}
