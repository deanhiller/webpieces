package org.webpieces.nio.impl.threading;

import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.util.threading.SessionExecutor;

public class ThreadUDPChannel extends ThreadChannel implements UDPChannel {

	private UDPChannel channel;

	public ThreadUDPChannel(UDPChannel channel, SessionExecutor executor) {
		super(channel, executor);
		this.channel = channel;
	}

	@Override
	public void disconnect() {
		channel.disconnect();
	}

}
