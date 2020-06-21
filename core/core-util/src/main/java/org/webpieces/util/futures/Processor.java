package org.webpieces.util.futures;

import java.util.concurrent.CompletableFuture;

public interface Processor<T> {

	public CompletableFuture<Void> process(T item);
}
