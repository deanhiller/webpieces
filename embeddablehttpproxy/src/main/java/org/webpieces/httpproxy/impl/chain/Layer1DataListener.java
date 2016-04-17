package org.webpieces.httpproxy.impl.chain;

import java.nio.ByteBuffer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

import com.webpieces.httpparser.api.dto.KnownStatusCode;

@Singleton
public class Layer1DataListener implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(Layer1DataListener.class);
	
	@Inject
	private Layer3Parser processor;
	@Inject
	private LayerZSendBadResponse badResponse;
	
	@Override
	public void incomingData(Channel channel, ByteBuffer b){
		try {
			processor.deserialize(channel, b);
		} catch(Throwable e) {
			sendBadResponse(channel, e, KnownStatusCode.HTTP500);
		}
	}

	private void sendBadResponse(Channel channel, Throwable exc, KnownStatusCode http500) {
		try {
			badResponse.sendServerResponse(channel, exc, KnownStatusCode.HTTP500);
		} catch(Throwable e) {
			log.info("Could not send response to client", e);
		}
	}

	@Override
	public void farEndClosed(Channel channel) {
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.info("Failure on channel="+channel, e);
	}

}
