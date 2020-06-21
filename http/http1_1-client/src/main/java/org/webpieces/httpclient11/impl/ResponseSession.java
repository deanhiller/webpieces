package org.webpieces.httpclient11.impl;

import java.util.concurrent.CompletableFuture;

public class ResponseSession {

	private CompletableFuture<Void> processFuture = CompletableFuture.completedFuture(null);

	public CompletableFuture<Void> getProcessFuture() {
		return processFuture;
	}

	public void setProcessFuture(CompletableFuture<Void> future) {
		this.processFuture = future;
	}

}
