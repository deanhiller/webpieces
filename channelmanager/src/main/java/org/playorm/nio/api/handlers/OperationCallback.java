package org.playorm.nio.api.handlers;


import java.io.IOException;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;


public interface OperationCallback {

	public void finished(Channel c) throws IOException;
	
	public void failed(RegisterableChannel c, Throwable e);
	
}
