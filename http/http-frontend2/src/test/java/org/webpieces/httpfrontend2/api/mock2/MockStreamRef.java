package org.webpieces.httpfrontend2.api.mock2;

import org.webpieces.util.futures.XFuture;

import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.Cancel;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class MockStreamRef implements StreamRef {

	private boolean isCancelled;
	private XFuture<StreamWriter> writer;
	private CancelReason reason;
	
	public MockStreamRef(XFuture<StreamWriter> writer) {
		this.writer = writer;
	}

	public MockStreamRef(MockStreamWriter mockSw) {
		this.writer = XFuture.completedFuture(mockSw);
	}

	@Override
	public XFuture<StreamWriter> getWriter() {
		return writer;
	}

	@Override
	public XFuture<Void> cancel(CancelReason reason) {
		this.reason = reason;
		isCancelled = true;
		return XFuture.completedFuture(null);
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	public CancelReason getCancelInfo() {
		return reason;
	}

}
