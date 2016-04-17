package org.webpieces.util.futures;

public interface Promise<T, F> {

	public void setResult(T result);
	
	public void setFailure(F failure);
	
}
