package org.webpieces.httpclient;

import org.webpieces.util.futures.XFuture;

import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpStreamRef;

public class MyHttpStreamRef implements HttpStreamRef {

	private XFuture<HttpDataWriter> writer;

	public MyHttpStreamRef(XFuture<HttpDataWriter> writer) {
		super();
		this.writer = writer;
	}

	@Override
	public XFuture<HttpDataWriter> getWriter() {
		return writer;
	}

	@Override
	public XFuture<Void> cancel(Object reason) {
		return XFuture.completedFuture(null);
	}

}
