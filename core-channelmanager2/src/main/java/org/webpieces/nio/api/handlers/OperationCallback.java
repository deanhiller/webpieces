package org.webpieces.nio.api.handlers;


import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;


public interface OperationCallback {

	public void finished(Channel c);
	
	public void failed(RegisterableChannel c, Throwable e);
	
}
