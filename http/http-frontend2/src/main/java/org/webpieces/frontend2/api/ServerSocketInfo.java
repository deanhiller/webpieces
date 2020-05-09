package org.webpieces.frontend2.api;

import java.net.InetSocketAddress;

public class ServerSocketInfo {

	private InetSocketAddress localBoundAddress;
	private boolean isBackendSocket;

	public ServerSocketInfo(boolean isBackendSocket) {
		this.isBackendSocket = isBackendSocket;
	}

	public void setServerSocketAddress(InetSocketAddress localAddr) {
		this.localBoundAddress = localAddr;
	}

	public boolean isBackendSocket() {
		return isBackendSocket;
	}

	public InetSocketAddress getLocalBoundAddress() {
		return localBoundAddress;
	}
	
}
