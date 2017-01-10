package org.webpieces.frontend.impl;

import java.nio.ByteBuffer;

import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.AsyncDataListener;

public class SharedListenerImpl implements AsyncDataListener {

	private ProtocolType protocol = ProtocolType.UNKNOWN;
	
	public SharedListenerImpl(HttpRequestListener httpListener) {
	}

	@Override
	public void incomingData(Channel channel, ByteBuffer b) {
		//when a channel is SSL, we can tell right away IF ALPN is installed
		//boolean isHttp2 = channel.getAlpnDetails().isHttp2();
		switch (protocol) {
		case HTTP2:
			
			break;
		case HTTP1_1:
			
			break;
		case UNKNOWN:
			
			
			break;
		default:
			throw new IllegalStateException("Unknown protocol="+protocol);
		}
	}

	@Override
	public void farEndClosed(Channel channel) {
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
	public void connectionOpened(TCPChannel proxy, boolean isReadyForWrites) {
		
	}

}
