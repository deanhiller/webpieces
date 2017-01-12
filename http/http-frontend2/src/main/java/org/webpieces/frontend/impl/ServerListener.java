package org.webpieces.frontend.impl;

import java.nio.ByteBuffer;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.AsyncDataListener;

public class ServerListener implements AsyncDataListener {

	private static final String FRONTEND_SOCKET = "__frontendSocket";
	private Http1_1Handler http1_1Handler;
	private Http2Handler http2Handler;
	
	public ServerListener(
			Http1_1Handler http1_1Listener, 
			Http2Handler http2Listener) {
		this.http1_1Handler = http1_1Listener;
		this.http2Handler = http2Listener;
	}

	@Override
	public void incomingData(Channel channel, ByteBuffer b) {
		FrontendSocketImpl socket = getSocket(channel);
		switch (socket.getProtocol()) {
		case HTTP2:
			http2Handler.incomingData(socket, b);
			break;
		case HTTP1_1:
			http1_1Handler.incomingData(socket, b);
			break;
		case UNKNOWN:
			initialData(b, socket);
			break;
		default:
			throw new IllegalStateException("Unknown protocol="+socket.getProtocol());
		}
	}

	private void initialData(ByteBuffer b, FrontendSocketImpl socket) {
		InitiationResult initialData = http1_1Handler.initialData(socket, b);
		if(initialData == null)
			return; //nothing to do, we don't know protocol yet
		else if(initialData.getInitialStatus() == InitiationStatus.HTTP1_1) {
			socket.setProtocol(ProtocolType.HTTP1_1);
		} else if(initialData.getInitialStatus() == InitiationStatus.PREFACE) {
			socket.setProtocol(ProtocolType.HTTP2);
			http2Handler.initialize(socket);
		} else {
			throw new UnsupportedOperationException("Did not implement case="+initialData.getInitialStatus()+" yet");
		}
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
			http1_1Handler.farEndClosed(socket);
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
	public void applyBackPressure(Channel channel) {
	}

	@Override
	public void releaseBackPressure(Channel channel) {
	}

	@Override
	public void connectionOpened(TCPChannel channel, boolean isReadyForWrites) {
		//when a channel is SSL, we can tell right away IF ALPN is installed
		//boolean isHttp2 = channel.getAlpnDetails().isHttp2();

		
		FrontendSocketImpl socket = new FrontendSocketImpl(channel, ProtocolType.UNKNOWN);
		channel.getSession().put(FRONTEND_SOCKET, socket);

		http1_1Handler.socketOpened(socket, isReadyForWrites);
	}

	FrontendSocketImpl getSocket(Channel channel) {
		return (FrontendSocketImpl) channel.getSession().get(FRONTEND_SOCKET);
	}
	
}
