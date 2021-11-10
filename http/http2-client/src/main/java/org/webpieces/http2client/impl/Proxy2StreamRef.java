package org.webpieces.http2client.impl;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class Proxy2StreamRef implements StreamRef {

	private StreamRef ref;
	private XFuture<StreamWriter> writer;

	public Proxy2StreamRef(StreamRef ref, XFuture<StreamWriter> writer) {
		this.ref = ref;
		this.writer = writer;
	}

	@Override
	public XFuture<StreamWriter> getWriter() {
		return writer;
	}

	@Override
	public XFuture<Void> cancel(CancelReason reason) {
		if(ref != null)
			return ref.cancel(reason);
		
		return XFuture.completedFuture(null);
	}

}
