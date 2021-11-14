package org.webpieces.util.locking;

import org.webpieces.util.futures.XFuture;
import java.util.function.Supplier;

public class QueuedRequest<RESP> {

	private XFuture<RESP> future;
	private Supplier<XFuture<RESP>> processor;
	private long timeQueued;

	public QueuedRequest(
			XFuture<RESP> future,
			Supplier<XFuture<RESP>> processor,
			long timeQueued
	) {
		this.future = future;
		this.processor = processor;
		this.timeQueued = timeQueued;
	}

	public XFuture<RESP> getFuture() {
		return future;
	}

	public Supplier<XFuture<RESP>> getProcessor() {
		return processor;
	}

	public long getTimeQueued() {
		return timeQueued;
	}

}
