package org.webpieces.util.futures;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Future<T> {

	public boolean isComplete();
	
	public Future<T> setResultFunction(Consumer<T> resultFunction);
	
	public Future<T> setFailureFunction(Consumer<Failure> failureFunction);
	
	public Future<T> setCancelFunction(Consumer<String> cancelFunction);
	
	public void cancel(String reason);

	/**
	 * Make it so when this future is resolved, it resolves a promise that
	 * another client is listening to.  
	 * 
	 * @param promise
	 * @param object
	 */
	public <R> void chain(PromiseImpl<R> promise, Function<T, R> object);
}
