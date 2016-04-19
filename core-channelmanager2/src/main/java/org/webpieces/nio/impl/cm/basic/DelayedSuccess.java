package org.webpieces.nio.impl.cm.basic;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.exceptions.FailureInfo;
import org.webpieces.util.futures.PromiseImpl;

public class DelayedSuccess implements DelayedWritesCloses {

	private PromiseImpl<Channel, FailureInfo> promise;
	private BasChannelImpl channel;

	public DelayedSuccess(PromiseImpl<Channel, FailureInfo> impl, BasChannelImpl basChannelImpl) {
		this.promise = impl;
		this.channel = basChannelImpl;
	}

	@Override
	public boolean runDelayedAction(boolean isSelectorThread) {
		promise.setResult(channel);
		return true;
	}

}
