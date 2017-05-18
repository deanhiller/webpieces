package org.webpieces.frontend2.api;

import java.net.InetSocketAddress;

import org.webpieces.frontend2.impl.ProtocolType;

public class SocketInfo {

	private ProtocolType protocolType;
	private boolean isHttps;
	private InetSocketAddress localBoundAddress;

	public SocketInfo(ProtocolType protocolType, boolean isHttps) {
		this.protocolType = protocolType;
		this.isHttps = isHttps;
	}

	public void setBoundAddress(InetSocketAddress localAddr) {
		this.localBoundAddress = localAddr;
	}

	public ProtocolType getProtocolType() {
		return protocolType;
	}

	public boolean isHttps() {
		return isHttps;
	}

	public InetSocketAddress getLocalBoundAddress() {
		return localBoundAddress;
	}
	
}
