package org.webpieces.frontend2.api;

import java.net.InetSocketAddress;

public class ServerSocketInfo {

	private boolean isHttps;
	private InetSocketAddress localBoundAddress;

	public ServerSocketInfo(boolean isHttps) {
		this.isHttps = isHttps;
	}

	public void setServerSocketAddress(InetSocketAddress localAddr) {
		this.localBoundAddress = localAddr;
	}

	public boolean isHttps() {
		return isHttps;
	}

	public InetSocketAddress getLocalBoundAddress() {
		return localBoundAddress;
	}
	
}
