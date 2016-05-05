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
public class Layer2DataListener implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(Layer2DataListener.class);
	
	@Inject
	private Layer3Parser processor;
	@Inject
	private LayerZSendBadResponse badResponse;
	
	public void incomingData(Channel channel, ByteBuffer b){
		try {
			log.info("incoming data. size="+b.remaining()+" channel="+channel);
			processor.deserialize(channel, b);
		} catch(Throwable e) {
			log.warn("Exeption processing", e);
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

	public void farEndClosed(Channel channel) {
		processor.clientClosedChannel(channel);
	}

	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.info("Failure on channel="+channel, e);
	}

	@Override
	public void applyBackPressure(Channel channel) {
		log.warn("Need to apply backpressure", new RuntimeException());
	}

	@Override
	public void releaseBackPressure(Channel channel) {
	}

}
