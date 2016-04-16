package org.webpieces.nio.impl.cm.secure;

import java.io.IOException;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.handlers.OperationCallback;


public class SecProxyWriteHandler implements OperationCallback {

	private OperationCallback handler;
	private Channel channel;

	public SecProxyWriteHandler(Channel c, OperationCallback h) {
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
