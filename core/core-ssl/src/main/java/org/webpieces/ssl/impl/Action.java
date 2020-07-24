package org.webpieces.ssl.impl;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

public class Action {

	private String thread;
	private ActionEnum action;
	private HandshakeStatus engineStatus;

	public Action(String thread, ActionEnum action, SSLEngine sslEngine) {
		this.thread = thread;
		this.action = action;
		this.engineStatus = sslEngine.getHandshakeStatus();
	}

	@Override
	public String toString() {
		return "Action [thread=" + thread + ", action=" + action + ", engineStatus=" + engineStatus + "]";
	}
	

}
