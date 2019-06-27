package org.webpieces.frontend2.api;

import java.net.InetSocketAddress;

public class ServerSocketInfo {

	private boolean isForServingHttpsPages;
	private InetSocketAddress localBoundAddress;
	private boolean isBackendSocket;

	public ServerSocketInfo(boolean isForServingHttpsPages, boolean isBackendSocket) {
		this.isForServingHttpsPages = isForServingHttpsPages;
		this.isBackendSocket = isBackendSocket;
	}

	public void setServerSocketAddress(InetSocketAddress localAddr) {
		this.localBoundAddress = localAddr;
	}

	public boolean isForServingHttpsPages() {
		return isForServingHttpsPages;
	}

	public boolean isBackendSocket() {
		return isBackendSocket;
	}

	public InetSocketAddress getLocalBoundAddress() {
		return localBoundAddress;
	}
	
}
