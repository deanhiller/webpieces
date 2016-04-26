package org.webpieces.util.futures;

public interface Promise<T> {

	public void setResult(T result);
	
	public void setFailure(Failure failure);
	
}
