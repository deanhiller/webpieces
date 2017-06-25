package org.webpieces.webserver.test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.impl.util.ChannelSessionImpl;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class MockTcpChannel implements TCPChannel {

	private static final Logger log = LoggerFactory.getLogger(MockTcpChannel.class);
	
	private ChannelSession session = new ChannelSessionImpl();

	private DataListener dataListener;

	@Override
	public CompletableFuture<Void> write(ByteBuffer b) {
		return dataListener.incomingData(this, b);
	}

	@Override
	public CompletableFuture<Void> close() {
		dataListener.farEndClosed(this);
		return CompletableFuture.completedFuture(null);
	}
	
	@Override
	public CompletableFuture<Void> connect(SocketAddress addr, DataListener listener) {
		throw new UnsupportedOperationException("no needed");
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
		return session;
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
	public void setName(String string) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getChannelId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> bind(SocketAddress addr) {
		return CompletableFuture.completedFuture(null);
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

	public void setDataListener(DataListener dataListener) {
		this.dataListener = dataListener;
	}

}
