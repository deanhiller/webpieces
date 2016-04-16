package org.webpieces.nio.api.handlers;


import java.io.IOException;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;


public interface OperationCallback {

	public void finished(Channel c) throws IOException;
	
	public void failed(RegisterableChannel c, Throwable e);
	
}
