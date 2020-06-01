package org.webpieces.httpclient11.api;

import java.util.concurrent.CompletableFuture;

public interface HttpStreamRef {
	
	public CompletableFuture<HttpDataWriter> getWriter();
	
	public CompletableFuture<Void> cancel(Object reason);	

}
