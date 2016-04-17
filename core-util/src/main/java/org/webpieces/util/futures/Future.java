package org.webpieces.util.futures;

import java.util.function.Consumer;

public interface Future<T, F> {

	public Future<T,F> setResultFunction(Consumer<T> resultFunction);
	
	public Future<T,F> setFailureFunction(Consumer<F> failureFunction);
	
	public Future<T,F> setCancelFunction(Consumer<String> cancelFunction);
	
	public void cancel(String reason);
}
