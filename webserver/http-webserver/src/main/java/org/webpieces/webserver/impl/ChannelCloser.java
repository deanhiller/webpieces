package org.webpieces.webserver.impl;

import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;

public class ChannelCloser {

	public Void closeIfNeeded(HttpRequest request, ResponseOverrideSender channel) {
		Header connHeader = request.getHeaderLookupStruct().getHeader(KnownHeaderName.CONNECTION);
		boolean close = false;
		if(connHeader != null) {
			String value = connHeader.getValue();
			if(!"keep-alive".equals(value)) {
				close = true;
			}
		} else
			close = true;
		
		if(close)
			channel.close();
		
		return null;
	}

}
