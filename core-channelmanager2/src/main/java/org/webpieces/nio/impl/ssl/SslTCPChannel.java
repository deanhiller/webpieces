package org.webpieces.nio.impl.ssl;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.ssl.api.AsyncSSLEngine;
import org.webpieces.ssl.api.SslListener;

public class SslTCPChannel extends SslChannel implements TCPChannel {

	private final AsyncSSLEngine sslEngine;
	private final SslTryCatchListener clientDataListener;
	private TCPChannel realChannel;
	
	private DataListener socketDataListener = new SocketDataListener();
	private CompletableFuture<Channel> sslConnectfuture;
	private CompletableFuture<Channel> closeFuture;
	private SslListener sslListener = new OurSslListener();
	
	public SslTCPChannel(Function<SslListener, AsyncSSLEngine> function, SslTryCatchListener wrapListener) {
		sslEngine = function.apply(sslListener );
		this.clientDataListener = wrapListener;
	}

	public void init(TCPChannel realChannel) {
		this.realChannel = realChannel;
		super.init(realChannel);
	}
	
	@Override
	public CompletableFuture<Channel> connect(SocketAddress addr) {
		CompletableFuture<Channel> future = realChannel.connect(addr);
		
		return future.thenCompose( c -> beginHandshake());
	}

	private CompletableFuture<Channel> beginHandshake() {
		sslConnectfuture = new CompletableFuture<Channel>();
		sslEngine.beginHandshake();
		return sslConnectfuture;
	}
	
	@Override
	public CompletableFuture<Channel> write(ByteBuffer b) {
		return sslEngine.feedPlainPacket(b).thenApply(v -> this);
	}

	@Override
	public CompletableFuture<Channel> close() {
		closeFuture = new CompletableFuture<>();		
		sslEngine.close();
		return closeFuture;
	}

	private class OurSslListener implements SslListener {
		@Override
		public void encryptedLinkEstablished() {
			sslConnectfuture.complete(SslTCPChannel.this);
		}

		@Override
		public CompletableFuture<Void> packetEncrypted(ByteBuffer engineToSocketData) {
			return realChannel.write(engineToSocketData).thenApply(c -> empty());
		}
		
		public Void empty() {
			return null;
		}

		@Override
		public void sendEncryptedHandshakeData(ByteBuffer engineToSocketData) {
			realChannel.write(engineToSocketData);
			//we don't care about future as we won't write anything out anyways until we get
			//data back and we have not fired connected to client so he should also not be writing yet too
		}
		
		@Override
		public void packetUnencrypted(ByteBuffer out) {
			clientDataListener.incomingData(SslTCPChannel.this, out);
		}

		@Override
		public void runTask(Runnable r) {
			//we are multithreaded underneath anyways using SessionExecutor so
			//we mine as well run this on same thread.
			r.run();
		}

		@Override
		public void closed(boolean clientInitiated) {
			if(!clientInitiated)
				clientDataListener.farEndClosed(SslTCPChannel.this);
			else if(closeFuture == null)
				throw new RuntimeException("bug, this should not be possible");
			else
				closeFuture.complete(SslTCPChannel.this);
		}
	}
	
	private class SocketDataListener implements DataListener {
		@Override
		public void incomingData(Channel channel, ByteBuffer b) {
			sslEngine.feedEncryptedPacket(b);
		}
	
		@Override
		public void farEndClosed(Channel channel) {
			clientDataListener.farEndClosed(SslTCPChannel.this);
		}
	
		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
			clientDataListener.failure(SslTCPChannel.this, data, e);
		}
	
		@Override
		public void applyBackPressure(Channel channel) {
			clientDataListener.applyBackPressure(SslTCPChannel.this);
		}
	
		@Override
		public void releaseBackPressure(Channel channel) {
			clientDataListener.releaseBackPressure(SslTCPChannel.this);
		}
	}
	
	@Override
	public boolean getKeepAlive() {
		return realChannel.getKeepAlive();
	}

	@Override
	public void setKeepAlive(boolean b) {
		realChannel.setKeepAlive(b);
	}

	public DataListener getDataListener() {
		return socketDataListener;
	}

}
