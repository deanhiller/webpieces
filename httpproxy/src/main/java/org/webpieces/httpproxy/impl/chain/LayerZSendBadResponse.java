package org.webpieces.httpproxy.impl.chain;

import java.nio.ByteBuffer;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.ParseException;
import com.webpieces.httpparser.api.dto.HttpResponse;
import com.webpieces.httpparser.api.dto.KnownStatusCode;

public class LayerZSendBadResponse {

	private static final Logger log = LoggerFactory.getLogger(LayerZSendBadResponse.class);
	
	@Inject
	private HttpParser parser;
	
	public void sendBadServerResponse(Channel channel, Throwable e, KnownStatusCode http500) {
		log.warn("Exception occurred", e);
		
		HttpResponse response = new HttpResponse();
		
		byte[] payload = parser.marshalToBytes(response);
		
		ByteBuffer buffer = ByteBuffer.wrap(payload);
		try {
			channel.write(buffer);
		} 
	}

	public void sendBadClientResponse(Channel channel, ParseException e, KnownStatusCode http4xx) {
		//move down to debug level later on..
		log.info("Client screwed up", e);
		
		
	}

}
