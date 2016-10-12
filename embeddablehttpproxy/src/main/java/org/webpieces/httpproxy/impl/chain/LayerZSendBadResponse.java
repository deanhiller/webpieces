package org.webpieces.httpproxy.impl.chain;

import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpcommon.api.exceptions.HttpException;

public class LayerZSendBadResponse {

	//private static final Logger log = LoggerFactory.getLogger(LayerZSendBadResponse.class);
	
	public void sendServerResponse(ResponseSender responseSender, HttpException e) {
		responseSender.sendException(e);
	}

}
