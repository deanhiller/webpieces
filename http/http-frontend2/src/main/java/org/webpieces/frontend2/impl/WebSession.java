package org.webpieces.frontend2.impl;

import java.util.concurrent.CompletableFuture;

public class WebSession {

	//start out completed
	private CompletableFuture<Void> processFuture = CompletableFuture.completedFuture(null);

	public CompletableFuture<Void> getProcessFuture() {
		return processFuture;
	}

	public void setProcessFuture(CompletableFuture<Void> processFuture) {
		this.processFuture = processFuture;
	}

}
