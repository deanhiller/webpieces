package org.webpieces.httpfrontend2.api.adaptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpfrontend2.api.http2.ServerChannel;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;

class ClientChannel implements TCPChannel {

	private String id;
	private DataListener dataListener;
	private ServerChannel svrChannel;
	private ServerChannels svrChannels2;

	public ClientChannel(String id, ServerChannels svrChannels) {
		this.id = id;
		svrChannels2 = svrChannels;
	}

	@Override
	public CompletableFuture<Channel> connect(SocketAddress addr, DataListener listener) {
		AdaptorServerChannel svr = svrChannels2.lookup(addr);
		if(svr == null) {
			CompletableFuture<Channel> f = new CompletableFuture<Channel>();
			f.completeExceptionally(new IOException("no port="+addr+" was bound on server"));
			return f;
		}

		svrChannel = new ServerChannel(this, listener);
		
		ConnectionListener connListener = svr.getConnectionListener();
		CompletableFuture<DataListener> connected = connListener.connected(svrChannel, true);
		return connected.thenApply(l -> {
			dataListener = l;
			return this;
		});
	}

	@Override
	public CompletableFuture<Channel> write(ByteBuffer b) {
		if(dataListener == null) {
			CompletableFuture<Channel> f = new CompletableFuture<Channel>();
			f.completeExceptionally(new IOException("not connected yet"));
			return f;
		}
			
		dataListener.incomingData(svrChannel, b);
		return CompletableFuture.completedFuture(this);
	}

	@Override
	public CompletableFuture<Channel> close() {
		dataListener.farEndClosed(svrChannel);
		return CompletableFuture.completedFuture(this);
	}

	@Override
	public CompletableFuture<Channel> registerForReads() {
		return null;
	}

	@Override
	public CompletableFuture<Channel> unregisterForReads() {
		return null;
	}

	@Override
	public boolean isRegisteredForReads() {
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMaxBytesWriteBackupSize(int maxBytesBackup) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMaxBytesBackupSize() {
		// TODO Auto-generated method stub
		return 0;
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
	public void bind(SocketAddress addr) {
		// TODO Auto-generated method stub
		
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
	
}