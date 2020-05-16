package org.webpieces.nio.impl.threading;

import org.slf4j.MDC;
import org.webpieces.nio.api.channels.RegisterableChannel;

public class SessionRunnable implements Runnable {

	private Runnable runnable;
	private RegisterableChannel channel;

	public SessionRunnable(Runnable runnable, RegisterableChannel channel) {
		this.runnable = runnable;
		this.channel = channel;
	}

	@Override
	public void run() {
		try {
			MDC.put("socket", channel+"");
			runnable.run();
		} finally {
			MDC.put("socket", null);
		}
		
	}

}
