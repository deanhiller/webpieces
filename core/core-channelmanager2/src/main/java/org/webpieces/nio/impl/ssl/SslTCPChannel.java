package org.webpieces.nio.impl.ssl;

import java.io.InputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.SSLEngineFactoryWithHost;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.ssl.api.AsyncSSLEngine;
import org.webpieces.ssl.api.AsyncSSLFactory;
import org.webpieces.ssl.api.SSLMetrics;
import org.webpieces.ssl.api.SslListener;

public class SslTCPChannel extends SslChannel implements TCPChannel {

	private final static Logger log = LoggerFactory.getLogger(SslTCPChannel.class);
	private AsyncSSLEngine sslEngine;
	private SslTryCatchListener clientDataListener;
	private final TCPChannel realChannel;
	
	private SocketDataListener socketDataListener;
	private CompletableFuture<Void> closeFuture = new CompletableFuture<>();
	private OurSslListener sslListener;
	private SSLEngineFactory sslFactory;
	private BufferPool pool;
	private ClientHelloParser parser;
	
	public SslTCPChannel(Function<SslListener, AsyncSSLEngine> function, TCPChannel realChannel, SSLMetrics metrics) {
		super(realChannel);
		this.sslListener = new OurSslListener();
		this.socketDataListener = new SocketDataListener(metrics);
		sslEngine = function.apply(sslListener );
		this.realChannel = realChannel;
	}

	public SslTCPChannel(BufferPool pool, TCPChannel realChannel2, ConnectionListener connectionListener, SSLEngineFactory sslFactory, SSLMetrics metrics) {
		super(realChannel2);
		this.pool = pool;
		this.socketDataListener = new SocketDataListener(metrics);
		parser = new ClientHelloParser(pool);
		this.realChannel = realChannel2;
		this.sslListener = new OurSslListener(connectionListener);
		this.sslFactory = sslFactory;
	}

	@Override
	public CompletableFuture<Void> connect(SocketAddress addr, DataListener listener) {
		clientDataListener = new SslTryCatchListener(listener);
		CompletableFuture<Void> future = realChannel.connect(addr, socketDataListener);
		
		return future.thenCompose( c -> beginHandshake());
	}

	public SocketDataListener getSocketDataListener() {
		return socketDataListener;
	}

	private CompletableFuture<Void> beginHandshake() {
		CompletableFuture<Void> sslConnectfuture = new CompletableFuture<Void>();
		sslListener.setSslClientConnectFuture(sslConnectfuture);
		CompletableFuture<Void> future = sslEngine.beginHandshake();
		return future.thenCompose(v -> sslConnectfuture);
	}
	
	@Override
	public CompletableFuture<Void> write(ByteBuffer b) {
		if(b.remaining() == 0)
			throw new IllegalArgumentException("You must pass in bytebuffers that contain data.  b.remaining==0 in this buffer");
		return sslEngine.feedPlainPacket(b);
	}

	@Override
	public CompletableFuture<Void> close() {
		if(sslEngine == null) {
			//this happens in the case where encryption link was not yet established(or even started for that matter)
			//ie. HttpFrontend does a timeout on incoming client connections to the server so if someone connects to ssl, it
			//times out and closes it
			return realChannel.close();
		}
		sslEngine.close();
		return closeFuture.thenApply(v -> actuallyCloseSocket(SslTCPChannel.this, realChannel));
	}

	private Void actuallyCloseSocket(Channel sslChannel, Channel realChannel) {
		realChannel.close();
		return null;
	}
	
	private class OurSslListener implements SslListener {
		private ConnectionListener conectionListener;
		private CompletableFuture<Void> sslClientConnectFuture;

		public OurSslListener() {
		}

		public OurSslListener(ConnectionListener conectionListener) {
			if(conectionListener == null)
				throw new IllegalArgumentException("conectionLsitener is null");
			this.conectionListener = conectionListener;
		}

		public void setSslClientConnectFuture(CompletableFuture<Void> sslConnectfuture) {
			this.sslClientConnectFuture = sslConnectfuture;
		}
		
		@Override
		public void encryptedLinkEstablished() {
			if(sslClientConnectFuture != null)
				sslClientConnectFuture.complete(null);
			else {
				CompletableFuture<DataListener> future = conectionListener.connected(SslTCPChannel.this, true);
				if(!future.isDone())
					conectionListener.failed(SslTCPChannel.this, new IllegalArgumentException("Client did not return a datalistener"));
				try {
					clientDataListener = new SslTryCatchListener(future.get());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		@Override
		public CompletableFuture<Void> packetEncrypted(ByteBuffer engineToSocketData) {
			return realChannel.write(engineToSocketData);
		}
		
		@Override
		public CompletableFuture<Void> sendEncryptedHandshakeData(ByteBuffer engineToSocketData) {
			try {
				return realChannel.write(engineToSocketData);
			} catch(NioClosedChannelException e) {
				log.info("Remote end closed before handshake was finished.  (nothing we can do about that)");
				return CompletableFuture.completedFuture(null);
			}
		}
		
		@Override
		public CompletableFuture<Void> packetUnencrypted(ByteBuffer out) {
			return clientDataListener.incomingData(SslTCPChannel.this, out);
		}

		@Override
		public void closed(boolean clientInitiated) {
			closeFuture.complete(null);
			
			if(!clientInitiated)
				clientDataListener.farEndClosed(SslTCPChannel.this);
		}
	}
	
	private class SocketDataListener implements DataListener {
		
		private SSLMetrics sslMetrics;

		public SocketDataListener(SSLMetrics sslMetrics) {
			this.sslMetrics = sslMetrics;
		}
		
		@Override
		public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
			if(sslEngine == null) {
				b = setupSSLEngine(channel, b);
				
				//this is frustrating so we just ack this set of data as completed(no backpressure basically here)
				//since b will be null IF we did not receive the full bytes of the ssl clientHello packet
				if(b == null) //to ack, we send a completed future back is all
					return CompletableFuture.completedFuture(null); //not fully setup yet
			}
			
			return sslEngine.feedEncryptedPacket(b);
		}

		private ByteBuffer setupSSLEngine(Channel channel, ByteBuffer b) {
			try {
				return setupSSLEngineImpl(channel, b);
			} catch (SSLException e) {
				throw new RuntimeException(e);
			}
		}
		
		private ByteBuffer setupSSLEngineImpl(Channel channel, ByteBuffer b) throws SSLException {
			if(sslFactory instanceof SSLEngineFactoryWithHost) {
				SSLEngineFactoryWithHost sslFactoryWithHost = (SSLEngineFactoryWithHost) sslFactory;
				ParseResult result = parser.fetchServerNamesIfEntirePacketAvailable(b);
				List<String> sniServerNames = result.getNames();
				
				if(sniServerNames.size() == 0) {
					log.error("SNI servernames missing from client.  channel="+channel.getRemoteAddress());
				} else if(sniServerNames.size() > 1) {
					log.error("SNI servernames are too many. names="+sniServerNames+" channel="+channel.getRemoteAddress());
				}
				
				String host = sniServerNames.get(0);
				SSLEngine engine = sslFactoryWithHost.createSslEngine(host);
				sslEngine = AsyncSSLFactory.create(realChannel+"", engine, pool, sslListener, sslMetrics);
				return result.getBuffer(); // return the full accumulated packet(which may just be the buffer passed in above)
			} else {
				SSLEngine engine = sslFactory.createSslEngine();
				sslEngine = AsyncSSLFactory.create(realChannel+"", engine, pool, sslListener, sslMetrics);
				return b;
			}
		}
		
		@Override
		public void farEndClosed(Channel channel) {
			if(clientDataListener != null)
				clientDataListener.farEndClosed(SslTCPChannel.this);
		}
	
		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
			clientDataListener.failure(SslTCPChannel.this, data, e);
		}
	}
	
	public SSLEngine createSslEngine() {
		try {
			// Create/initialize the SSLContext with key material
	
			InputStream in = getClass().getClassLoader().getResourceAsStream("selfsigned.jks");
			
			char[] passphrase = "password".toCharArray();
			// First initialize the key and trust material.
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(in, passphrase);
			SSLContext sslContext = SSLContext.getInstance("TLS");
			
			//****************Server side specific*********************
			// KeyManager's decide which key material to use.
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, passphrase);
			sslContext.init(kmf.getKeyManagers(), null, null);		
			//****************Server side specific*********************
			
			SSLEngine engine = sslContext.createSSLEngine();
			engine.setUseClientMode(false);
			
			return engine;
		} catch(Exception e) {
			throw new RuntimeException(e);
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

	@Override
	public boolean isSslChannel() {
		return true;
	}

}
