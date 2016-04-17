package org.webpieces.util.futures;

import java.util.function.Consumer;

public class PromiseImpl<T, F> implements Future<T, F>, Promise<T, F> {

	private boolean complete = false;
	private Consumer<T> resultFunc;
	private Consumer<F> failureFunc;
	private T result;
	private F failure;
	private Consumer<String> cancelFunc;
	private String cancelReason;

	public void setResult(T result) {
		if(result == null)
			throw new IllegalArgumentException("param cannot be null(this operates off null checks)");
		this.result = result;
		fire(resultFunc, result);
	}
	
	public void setFailure(F failure) {
		if(failure == null)
			throw new IllegalArgumentException("param cannot be null(this operates off null checks)");
		this.failure = failure;
		fire(failureFunc, failure);
	}
	
	@Override
	public void cancel(String reason) {
		if(reason == null)
			throw new IllegalArgumentException("param cannot be null(this operates off null checks)");
		this.cancelReason = reason;
		fire(cancelFunc, cancelReason);
	}
	
	public PromiseImpl<T,F> setResultFunction(Consumer<T> resultFunction) {
		if(resultFunction == null)
			throw new IllegalArgumentException("param cannot be null(this operates off null checks)");
		this.resultFunc = resultFunction;
		fire(resultFunc, result);
		return this;
	}
	
	public PromiseImpl<T,F> setFailureFunction(Consumer<F> failureFunction) {
		if(failureFunction == null)
			throw new IllegalArgumentException("param cannot be null(this operates off null checks)");
		this.failureFunc = failureFunction;
		fire(failureFunc, failure);
		return this;
	}

	@Override
	public Future<T, F> setCancelFunction(Consumer<String> cancelFunction) {
		if(cancelFunction == null)
			throw new IllegalArgumentException("param cannot be null(this operates off null checks)");
		this.cancelFunc = cancelFunction;
		fire(cancelFunc, cancelReason);
		return this;
	}

	private <E> void fire(Consumer<E> consumer, E result) {
		if(consumer == null || result == null)
			return;
		
		synchronized (this) {
			if(complete) //only allow completing once
				return;
			
			complete = true;
		}
		
		//done outside synchronization block..
		consumer.accept(result);
	}
	
}
