package org.webpieces.webserver.test.http2.directfast;

import java.net.InetSocketAddress;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.impl.ProtocolType;
import org.webpieces.nio.api.channels.ChannelSession;

public class MockFrontendSocket implements FrontendSocket {

	private boolean isHttps;

	public MockFrontendSocket(boolean isHttps) {
		this.isHttps = isHttps;
	}
	
	@Override
	public void close(String reason) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ChannelSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProtocolType getProtocol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isForServingHttpsPages() {
		return isHttps;
	}

	@Override
	public boolean isBackendSocket() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InetSocketAddress getServerLocalBoundAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		// TODO Auto-generated method stub
		return null;
	}

}
