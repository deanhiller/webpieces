package org.webpieces.router.impl.routeinvoker;

import org.webpieces.util.futures.XFuture;
import java.util.function.Function;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class RouterStreamRef implements StreamRef, Function<CancelReason, XFuture<Void>> {

	private XFuture<StreamWriter> writer;
	private Function<CancelReason, XFuture<Void>> cancelFunc;
	private String id; //can be used in tracing/debugging as it's hard to realize which ref came from where

	public RouterStreamRef(String id, XFuture<StreamWriter> writer, Function<CancelReason, XFuture<Void>> cancelFunc) {
		this.id = id;
		this.writer = writer;
		this.cancelFunc = cancelFunc;
	}

	public RouterStreamRef(String id) {
		this.id = id;
		this.writer = XFuture.completedFuture(new NullWriter());
	}
	
	public RouterStreamRef(String id, Throwable e) {
		this.id = id;
		this.writer = new XFuture<StreamWriter>();
		this.writer.completeExceptionally(e);
	}

	@Override
	public XFuture<StreamWriter> getWriter() {
		return writer;
	}

	@Override
	public XFuture<Void> cancel(CancelReason reason) {
		if(cancelFunc != null)
			return cancelFunc.apply(reason);
		
		return XFuture.completedFuture(null);
	}
	
	public RouterStreamRef thenApply(String id, Function<StreamWriter, StreamWriter> fn) {
		XFuture<StreamWriter> newWriter = writer.thenApply(fn);
		return new RouterStreamRef(id, newWriter, this);
	}

	@Override
	public XFuture<Void> apply(CancelReason t) {
		return cancel(t);
	}

	@Override
	public String toString() {
		return "RouterStreamRef [id=" + id + "]";
	}
	
}
