package org.webpieces.router.impl.routeinvoker;

public class PortAndIsSecure {

	private int port;
	private boolean isSecure;

	public PortAndIsSecure(int port, boolean isSecure) {
		this.port = port;
		this.isSecure = isSecure;
	}

	public Integer getPort() {
		return port;
	}

	public boolean isSecure() {
		return isSecure;
	}
	
}
