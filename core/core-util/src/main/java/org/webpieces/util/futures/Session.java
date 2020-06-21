package org.webpieces.util.futures;

import java.util.concurrent.CompletableFuture;

public interface Session {

	void setProcessFuturee(CompletableFuture<Void> future);

	CompletableFuture<Void> getProcessFuture();

}
