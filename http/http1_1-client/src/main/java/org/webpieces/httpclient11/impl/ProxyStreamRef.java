package org.webpieces.httpclient11.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpStreamRef;

public class ProxyStreamRef implements HttpStreamRef {

	private HttpStreamRef ref;
	private CompletableFuture<HttpDataWriter> newWriter;

	public ProxyStreamRef(HttpStreamRef ref, CompletableFuture<HttpDataWriter> newWriter) {
		this.ref = ref;
		this.newWriter = newWriter;
	}

	@Override
	public CompletableFuture<HttpDataWriter> getWriter() {
		return newWriter;
	}

	@Override
	public CompletableFuture<Void> cancel(Object reason) {
		if(ref != null)
			return ref.cancel(reason);
		return CompletableFuture.completedFuture(null);
	}

}
