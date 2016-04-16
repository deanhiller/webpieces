package org.webpieces.httpproxy.impl.chain;

import java.nio.ByteBuffer;

import javax.inject.Inject;

import org.webpieces.nio.api.channels.Channel;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.dto.HttpResponse;
import com.webpieces.httpparser.api.dto.HttpResponseStatus;
import com.webpieces.httpparser.api.dto.HttpResponseStatusLine;
import com.webpieces.httpparser.api.dto.KnownStatusCode;

public class LayerZSendBadResponse {

	//private static final Logger log = LoggerFactory.getLogger(LayerZSendBadResponse.class);
	
	@Inject
	private HttpParser parser;
	
	public void sendServerResponse(Channel channel, Throwable e, KnownStatusCode statusCode) {
		HttpResponseStatus respStatus = new HttpResponseStatus();
		respStatus.setKnownStatus(statusCode);
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(respStatus);
		
		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine );
		
		byte[] payload = parser.marshalToBytes(response);
		
		ByteBuffer buffer = ByteBuffer.wrap(payload);
		channel.write(buffer);
	}

}
