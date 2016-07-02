package org.webpieces.nio.impl.threading;

import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.util.threading.SessionExecutor;

public class ThreadTCPChannel extends ThreadChannel implements TCPChannel {

	private TCPChannel channel;

	public ThreadTCPChannel(TCPChannel channel, SessionExecutor executor) {
		super(channel, executor);
		this.channel = channel;
	}

	@Override
	public boolean getKeepAlive() {
		return channel.getKeepAlive();
	}

	@Override
	public void setKeepAlive(boolean b) {
		channel.setKeepAlive(b);
	}

}
