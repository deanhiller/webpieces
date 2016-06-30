package org.webpieces.nio.impl.threading;

import java.util.concurrent.Executor;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.util.threading.SessionExecutor;

public class ProxyExecutor implements Executor {

	private Channel channel;
	private SessionExecutor executor;

	public ProxyExecutor(Channel channel, SessionExecutor executor) {
		this.channel = channel;
		this.executor = executor;
	}

	@Override
	public void execute(Runnable r) {
		executor.execute(channel, r);
	}

}
