package org.webpieces.webserver.test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.webpieces.util.HostWithPort;
import org.webpieces.util.futures.XFuture;

import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.impl.util.ChannelSessionImpl;

public class MockTcpChannel implements TCPChannel {

	private ChannelSession session = new ChannelSessionImpl();

	private DataListener dataListener;
	private boolean isSsl;

	public MockTcpChannel(boolean isSsl) {
		this.isSsl = isSsl;
	}

	@Override
	public XFuture<Void> write(ByteBuffer b) {
		return dataListener.incomingData(this, b);
	}

	@Override
	public XFuture<Void> close() {
		dataListener.farEndClosed(this);
		return XFuture.completedFuture(null);
	}

	@Override
	public XFuture<Void> connect(HostWithPort addr, DataListener listener) {
		throw new UnsupportedOperationException("no needed");
	}

	/**
	 * @deprecated Use connect(HostWithPort, DataListener) or connect(IpWithPort, DataListener) instead
	 */
	@Deprecated
	@Override
	public XFuture<Void> connect(SocketAddress addr, DataListener listener) {
		throw new UnsupportedOperationException("no needed");
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return null;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public ChannelSession getSession() {
		return session;
	}

	@Override
	public boolean isSslChannel() {
		return isSsl;
	}

	@Override
	public void setReuseAddress(boolean b) {
	}

	@Override
	public String getChannelId() {
		return null;
	}

	@Override
	public XFuture<Void> bind(SocketAddress addr) {
		return XFuture.completedFuture(null);
	}

	@Override
	public boolean isBlocking() {
		
		return false;
	}

	@Override
	public boolean isClosed() {
		
		return false;
	}

	@Override
	public boolean isBound() {
		
		return false;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		
		return null;
	}

	@Override
	public boolean getKeepAlive() {
		
		return false;
	}

	@Override
	public void setKeepAlive(boolean b) {
		

	}

	public void setDataListener(DataListener dataListener) {
		this.dataListener = dataListener;
	}

	@Override
	public Boolean isServerSide() {
		// TODO Auto-generated method stub
		return null;
	}

}
