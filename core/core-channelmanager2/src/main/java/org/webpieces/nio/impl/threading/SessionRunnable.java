package org.webpieces.nio.impl.threading;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.impl.cm.basic.MDCUtil;

public class SessionRunnable implements Runnable {

	private Runnable runnable;
	private RegisterableChannel channel;
	private Boolean isServerSide;

	public SessionRunnable(Runnable runnable, RegisterableChannel channel) {
		this.runnable = runnable;
		this.channel = channel;
		if(channel instanceof Channel) {
			this.isServerSide = ((Channel)channel).isServerSide();
		}
	}

	@Override
	public void run() {
		try {
			MDCUtil.setMDC(isServerSide, channel.getChannelId());

			runnable.run();
		} finally {
			MDCUtil.setMDC(isServerSide, channel.getChannelId());
		}
	}

}
