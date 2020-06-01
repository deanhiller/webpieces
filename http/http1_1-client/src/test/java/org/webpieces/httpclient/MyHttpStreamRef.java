package org.webpieces.httpclient;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpStreamRef;

public class MyHttpStreamRef implements HttpStreamRef {

	private CompletableFuture<HttpDataWriter> writer;

	public MyHttpStreamRef(CompletableFuture<HttpDataWriter> writer) {
		super();
		this.writer = writer;
	}

	@Override
	public CompletableFuture<HttpDataWriter> getWriter() {
		return writer;
	}

	@Override
	public CompletableFuture<Void> cancel(Object reason) {
		return CompletableFuture.completedFuture(null);
	}

}
