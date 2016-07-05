package org.webpieces.httpproxy.impl.chain;

import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.frontend.api.exception.HttpException;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpResponseStatus;
import org.webpieces.httpparser.api.dto.HttpResponseStatusLine;

public class LayerZSendBadResponse {

	//private static final Logger log = LoggerFactory.getLogger(LayerZSendBadResponse.class);
	
	public void sendServerResponse(FrontendSocket channel, HttpException e) {
		HttpResponseStatus respStatus = new HttpResponseStatus();
		respStatus.setKnownStatus(e.getStatusCode());
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(respStatus);
		
		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine );

		response.addHeader(new Header("Failure", e.getMessage()));
		
		channel.write(response);
	}

}
