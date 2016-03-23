package org.webpieces.httpproxy.impl.chain;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataChunk;
import org.webpieces.nio.api.handlers.DataListener;

import com.webpieces.httpparser.api.dto.KnownStatusCode;

@Singleton
public class Layer1DataListener implements DataListener {

	@Inject
	private Layer3Parser processor;
	@Inject
	private LayerZSendBadResponse badResponse;
	
	@Override
	public void incomingData(Channel channel, DataChunk b) throws IOException {
		try {
			processor.deserialize(channel, b);
		} catch(Throwable e) {
			badResponse.sendBadServerResponse(channel, e, KnownStatusCode.HTTP500);
		}
	}

	@Override
	public void farEndClosed(Channel channel) {
		
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		
	}

}
