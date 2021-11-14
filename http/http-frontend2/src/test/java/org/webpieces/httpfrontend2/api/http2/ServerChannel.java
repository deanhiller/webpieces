package org.webpieces.httpfrontend2.api.http2;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.impl.util.ChannelSessionImpl;

public class ServerChannel implements TCPChannel {

	private DataListener listener;
	private TCPChannel clientChannel;
	private ChannelSession channelSession = new ChannelSessionImpl();

	public ServerChannel(TCPChannel clientChannel, DataListener listener) {
		this.clientChannel = clientChannel;
		this.listener = listener;
	}

	@Override
	public XFuture<Void> connect(SocketAddress addr, DataListener listener) {
		throw new IllegalStateException("should be already connected");
	}

	@Override
	public XFuture<Void> write(ByteBuffer b) {
		listener.incomingData(clientChannel, b);
		return XFuture.completedFuture(null);
	}

	@Override
	public XFuture<Void> close() {
		listener.farEndClosed(clientChannel);
		return XFuture.completedFuture(null);
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
		return channelSession ;
	}

	@Override
	public boolean isSslChannel() {
		
		return false;
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

	@Override
	public Boolean isServerSide() {
		return clientChannel.isServerSide();
	}

}
