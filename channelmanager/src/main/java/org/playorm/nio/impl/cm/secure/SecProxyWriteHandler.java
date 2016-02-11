package org.playorm.nio.impl.cm.secure;

import java.io.IOException;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.handlers.OperationCallback;


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
