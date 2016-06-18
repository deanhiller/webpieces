package org.webpieces.httpproxy.impl.chain;

import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpResponseStatus;
import org.webpieces.httpparser.api.dto.HttpResponseStatusLine;
import org.webpieces.httpparser.api.dto.KnownStatusCode;

public class LayerZSendBadResponse {

	//private static final Logger log = LoggerFactory.getLogger(LayerZSendBadResponse.class);
	
	public void sendServerResponse(FrontendSocket channel, Throwable e, KnownStatusCode statusCode) {
		HttpResponseStatus respStatus = new HttpResponseStatus();
		respStatus.setKnownStatus(statusCode);
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(respStatus);
		
		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine );

		channel.write(response);
	}

}
