package org.webpieces.httpclient11.impl;

import org.webpieces.util.futures.XFuture;

import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpStreamRef;

public class ProxyStreamRef implements HttpStreamRef {

	private HttpStreamRef ref;
	private XFuture<HttpDataWriter> newWriter;

	public ProxyStreamRef(HttpStreamRef ref, XFuture<HttpDataWriter> newWriter) {
		this.ref = ref;
		this.newWriter = newWriter;
	}

	@Override
	public XFuture<HttpDataWriter> getWriter() {
		return newWriter;
	}

	@Override
	public XFuture<Void> cancel(Object reason) {
		if(ref != null)
			return ref.cancel(reason);
		return XFuture.completedFuture(null);
	}

}
