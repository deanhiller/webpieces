package org.webpieces.nio.impl.util;


import java.io.IOException;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.handlers.OperationCallback;


public class UtilPassThroughWriteHandler implements OperationCallback {

	private Channel channel;
	private OperationCallback handler;

	public UtilPassThroughWriteHandler(Channel c, OperationCallback h) {
		if(c == null || h == null)
			throw new IllegalArgumentException(c+"Niether c nor h parameters can be null");
		channel = c;
		handler = h;
	}
	public void finished(Channel realChannel) throws IOException {
		handler.finished(channel);
	}

	public void failed(RegisterableChannel realChannel, Throwable e) {
		handler.failed(channel, e);
	}

}
