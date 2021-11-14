package com.webpieces.http2.api.streaming;

import java.util.concurrent.CancellationException;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;

public class MyStreamRef implements StreamRef {

	private AtomicReference<XFuture<StreamWriter>> ref = new AtomicReference<XFuture<StreamWriter>>();

	public MyStreamRef(XFuture<StreamWriter> writer) {
		ref.set(writer);
	}
	
	@Override
	public XFuture<StreamWriter> getWriter() {
		return ref.get();
	}

	@Override
	public XFuture<Void> cancel(CancelReason reason) {
		//swap out writer for a cancelled one
		XFuture<StreamWriter> writer = new XFuture<StreamWriter>();
		writer.completeExceptionally(new CancellationException("Cancelled.  reason="+reason));
		ref.set(writer);
		
		return XFuture.completedFuture(null);
	}

}
