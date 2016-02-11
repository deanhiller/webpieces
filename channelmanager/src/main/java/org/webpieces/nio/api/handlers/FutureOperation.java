package org.webpieces.nio.api.handlers;

public interface FutureOperation {

	public void waitForOperation(long timeoutInMillis);
	
	public void waitForOperation();
	
	public void setListener(OperationCallback cb);
	
}
