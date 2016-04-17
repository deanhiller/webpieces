package org.webpieces.nio.api.exceptions;

import org.webpieces.nio.api.channels.RegisterableChannel;

public class FailureInfo {

	private RegisterableChannel channel;
	private Throwable exception;

	public FailureInfo(RegisterableChannel channel, Throwable e) {
		this.channel = channel;
		this.exception = e;
	}

	public RegisterableChannel getChannel() {
		return channel;
	}

	public Throwable getException() {
		return exception;
	}
	
}
