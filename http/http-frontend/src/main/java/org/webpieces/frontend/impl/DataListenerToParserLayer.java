package org.webpieces.frontend.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

public class DataListenerToParserLayer implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(DataListenerToParserLayer.class);
	
	private ParserLayer processor;
	
	public DataListenerToParserLayer(ParserLayer nextStage) {
		this.processor = nextStage;
	}

	public void incomingData(Channel channel, ByteBuffer b, boolean isOpeningConnection){
		try {
			if(isOpeningConnection) {
				processor.openedConnection(channel);
				return;
			}
			InetSocketAddress addr = channel.getRemoteAddress();
			channel.setName(""+addr);
			log.info("incoming data. size="+b.remaining()+" channel="+channel);
			processor.deserialize(channel, b);
		} catch(Throwable e) {
			log.error("Exeption processing", e);
			sendBadResponse(channel, e, KnownStatusCode.HTTP500);
		}
	}

	private void sendBadResponse(Channel channel, Throwable exc, KnownStatusCode http500) {
		try {
			processor.sendServerResponse(channel, exc, KnownStatusCode.HTTP500);
		} catch(Throwable e) {
			log.info("Could not send response to client", e);
		}
	}

	public void farEndClosed(Channel channel) {
		log.info("far end closed. channel="+channel);
		processor.farEndClosed(channel);
	}

	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.info("Failure on channel="+channel, e);
		channel.close();
	}

	@Override
	public void applyBackPressure(Channel channel) {
		log.error("Need to apply backpressure", new RuntimeException("demonstrates how we got here"));
		processor.applyWriteBackPressure(channel);
	}

	@Override
	public void releaseBackPressure(Channel channel) {
		log.info("can release backpressure");
		processor.releaseBackPressure(channel);
	}

}
