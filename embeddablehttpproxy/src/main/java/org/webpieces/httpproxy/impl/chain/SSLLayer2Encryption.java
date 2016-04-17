package org.webpieces.httpproxy.impl.chain;

import java.nio.ByteBuffer;

import javax.inject.Inject;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

public class SSLLayer2Encryption implements DataListener {

	@Inject
	private Layer3Parser processor;
	
	@Override
	public void incomingData(Channel channel, ByteBuffer b) {
		
	}

	@Override
	public void farEndClosed(Channel channel) {
		
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		
	}

}
