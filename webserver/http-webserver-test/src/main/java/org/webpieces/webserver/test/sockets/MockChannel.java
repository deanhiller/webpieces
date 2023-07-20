package org.webpieces.webserver.test.sockets;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.webpieces.nio.api.channels.HostWithPort;
import org.webpieces.util.futures.XFuture;

import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;

public class MockChannel implements TCPChannel {

	private ChannelSession session = new MyChannelSession();

	@Override
	public XFuture<Void> connect(HostWithPort addr, DataListener listener) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @deprecated Use connect(HostWithPort, DataListener) or connect(IpWithPort, DataListener) instead
	 */
	@Deprecated
	@Override
	public XFuture<Void> connect(SocketAddress addr, DataListener listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XFuture<Void> write(ByteBuffer b) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XFuture<Void> close() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ChannelSession getSession() {
		return session ;
	}

	@Override
	public boolean isSslChannel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReuseAddress(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getChannelId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XFuture<Void> bind(SocketAddress addr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBound() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getKeepAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setKeepAlive(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Boolean isServerSide() {
		return null;
	}


}
