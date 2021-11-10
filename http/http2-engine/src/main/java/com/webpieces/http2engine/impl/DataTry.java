package com.webpieces.http2engine.impl;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class DataTry {

	private Stream stream;
	private DataFrame dataFrame;
	private XFuture<Void> future;
	private boolean wasQueuedBefore;

	public DataTry(Stream stream, DataFrame dataFrame, XFuture<Void> future, boolean wasQueuedBefore) {
		this.stream = stream;
		this.dataFrame = dataFrame;
		this.future = future;
		this.setWasQueuedBefore(wasQueuedBefore);
	}

	public Stream getStream() {
		return stream;
	}

	public DataFrame getDataFrame() {
		return dataFrame;
	}

	public XFuture<Void> getFuture() {
		return future;
	}

	public boolean isWasQueuedBefore() {
		return wasQueuedBefore;
	}

	public void setWasQueuedBefore(boolean wasQueuedBefore) {
		this.wasQueuedBefore = wasQueuedBefore;
	}
	
}
