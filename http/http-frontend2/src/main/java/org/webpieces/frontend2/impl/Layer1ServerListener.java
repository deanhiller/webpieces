package org.webpieces.frontend2.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.asyncserver.api.AsyncDataListener;
import org.webpieces.frontend2.api.ServerSocketInfo;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.util.futures.ExceptionUtil;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class Layer1ServerListener implements AsyncDataListener {
	private static final Logger log = LoggerFactory.getLogger(Layer1ServerListener.class);

	private static final String FRONTEND_SOCKET = "__frontendSocket";
	private Layer2Http11Handler http11Handler;
	private Layer2Http2Handler http2Handler;

	private ServerSocketInfo svrSocketInfo;
	//private MsgRateRecorder recorder = new MsgRateRecorder(10, "bytes/second");

	public Layer1ServerListener(
			Layer2Http11Handler http11Listener, 
			Layer2Http2Handler http2Listener,
			boolean isHttps, 
			boolean isBackendRequest
	) {
		this.http11Handler = http11Listener;
		this.http2Handler = http2Listener;
		svrSocketInfo = new ServerSocketInfo(isHttps, isBackendRequest);
	}

	@Override
	public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		//recorder.increment(b.remaining());
		FrontendSocketImpl socket = getSocket(channel);
		switch (socket.getProtocol()) {
		case HTTP2:
			return http2Handler.incomingData(socket, b);
		case HTTP1_1:
			return http11Handler.incomingData(socket, b);
		case UNKNOWN:
			return initialData(b, socket);
		default:
			throw new IllegalStateException("Unknown protocol="+socket.getProtocol());
		}
	}

	private CompletableFuture<Void> initialData(ByteBuffer b, FrontendSocketImpl socket) {
		
		CompletableFuture<InitiationResult> future = ExceptionUtil.wrap( () -> http11Handler.initialData(socket, b));
		future.exceptionally( t -> {
			socket.close("reason not needed");
			return null;
		});

		return future.thenCompose( initialData -> {
		
			if(initialData == null)
				return CompletableFuture.completedFuture(null); //nothing to do, we don't know protocol yet
			else if(initialData.getInitialStatus() == InitiationStatus.HTTP1_1) {
				socket.setProtocol(ProtocolType.HTTP1_1);
				return CompletableFuture.completedFuture(null);
			} else if(initialData.getInitialStatus() == InitiationStatus.PREFACE) {
				socket.setProtocol(ProtocolType.HTTP2);
				CompletableFuture<Void> initFuture = http2Handler.initialize(socket);
				
				//process any leftover data next
				CompletableFuture<Void> dataFuture = http2Handler.incomingData(socket, initialData.getLeftOverData());
				
				return initFuture.thenCompose(s -> dataFuture);
				
			} else {
				throw new UnsupportedOperationException("Did not implement case="+initialData.getInitialStatus()+" yet");
			}
		
		});
		
		
	}

	@Override
	public void farEndClosed(Channel channel) {
		FrontendSocketImpl socket = getSocket(channel);

		//when a channel is SSL, we can tell right away IF ALPN is installed
		//boolean isHttp2 = channel.getAlpnDetails().isHttp2();
		switch (socket.getProtocol()) {
		case HTTP2:
			http2Handler.farEndClosed(socket);
			break;
		case HTTP1_1:
			http11Handler.farEndClosed(socket);
			break;
		case UNKNOWN:
			//timeoutHandler.connectionClosedBeforeRequest(socket);
			
			break;
		default:
			throw new IllegalStateException("Unknown protocol="+socket.getProtocol());
		}
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
	}

	@Override
	public void connectionOpened(TCPChannel channel, boolean isReadyForWrites) {
		if(log.isDebugEnabled())
			log.debug(channel+" socket opened");
		//when a channel is SSL, we can tell right away IF ALPN is installed
		//boolean isHttp2 = channel.getAlpnDetails().isHttp2();

		FrontendSocketImpl socket = new FrontendSocketImpl(channel, ProtocolType.UNKNOWN, svrSocketInfo);
		channel.getSession().put(FRONTEND_SOCKET, socket);

		http11Handler.socketOpened(socket, isReadyForWrites);
	}

	FrontendSocketImpl getSocket(Channel channel) {
		return (FrontendSocketImpl) channel.getSession().get(FRONTEND_SOCKET);
	}

	public void setSvrSocketAddr(InetSocketAddress localAddr) {
		svrSocketInfo.setServerSocketAddress(localAddr);
	}
	
}
