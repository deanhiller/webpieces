package org.webpieces.webserver.impl;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class ChannelCloser {

	public Void closeIfNeeded(Http2Headers request, ResponseOverrideSender channel) {
		Http2Header connHeader = request.getHeaderLookupStruct().getHeader(Http2HeaderName.CONNECTION);
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
