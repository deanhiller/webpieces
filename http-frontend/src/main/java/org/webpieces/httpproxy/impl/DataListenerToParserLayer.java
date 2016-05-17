package org.webpieces.httpproxy.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpproxy.api.HttpRequestListener;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

import com.webpieces.httpparser.api.dto.KnownStatusCode;

public class DataListenerToParserLayer implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(DataListenerToParserLayer.class);
	
	private ParserLayer processor;
	
	public DataListenerToParserLayer(ParserLayer nextStage) {
		this.processor = nextStage;
	}

	public void incomingData(Channel channel, ByteBuffer b){
		try {
			InetSocketAddress addr = channel.getRemoteAddress();
			channel.setName(""+addr);
			log.info("incoming data. size="+b.remaining()+" channel="+channel);
			processor.deserialize(channel, b);
		} catch(Throwable e) {
			log.warn("Exeption processing", e);
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
		processor.farEndClosed(channel);
	}

	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.info("Failure on channel="+channel, e);
		channel.close();
	}

	@Override
	public void applyBackPressure(Channel channel) {
		log.warn("Need to apply backpressure", new RuntimeException("demonstrates how we got here"));
		processor.applyWriteBackPressure(channel);
	}

	@Override
	public void releaseBackPressure(Channel channel) {
		processor.releaseBackPressure(channel);
	}

}
