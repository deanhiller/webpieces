package org.webpieces.frontend.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.webpieces.httpcommon.api.exceptions.HttpClientException;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpcommon.api.exceptions.HttpServerException;
import org.webpieces.httpparser.api.ParseException;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.logging.SupressedExceptionLog;

class Http11DataListener implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(Http11DataListener.class);
	
	private Http11Layer processor;
	
	Http11DataListener(Http11Layer nextStage) {
		this.processor = nextStage;
	}
	
	@Override
	public void incomingData(Channel channel, ByteBuffer b){
		try {
			InetSocketAddress addr = channel.getRemoteAddress();
			channel.setName(""+addr);
			log.trace(()->"incoming data. size="+b.remaining()+" channel="+channel);
			processor.deserialize(channel, b);
		} catch(ParseException e) {
			HttpClientException exc = new HttpClientException("Could not parse http request", KnownStatusCode.HTTP_400_BADREQUEST, e);
			//move down to debug level later on..
			log.info("Client screwed up", exc);
			SupressedExceptionLog.log(exc); //next log secondary exceptions
			sendBadResponse(channel, exc);
		} catch(Throwable e) {
			HttpServerException exc = new HttpServerException("There was a bug in the server, please see the server logs", KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR, e);
			log.error("Exception processing", exc);
			SupressedExceptionLog.log(exc);
			sendBadResponse(channel, exc);
		}
	}

	private void sendBadResponse(Channel channel, HttpException exc) {
		try {
			processor.sendServerException(channel, exc);
		} catch(Throwable e) {
			log.info("Could not send response to client", e);
		}
	}

	@Override
	public void farEndClosed(Channel channel) {
		log.trace(()->"far end closed. channel="+channel);
		processor.farEndClosed(channel);
	}

	@Override
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
