package org.webpieces.router.impl.proxyout;

import javax.inject.Singleton;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

@Singleton
public class ChannelCloser {

	public Void closeIfNeeded(Http2Headers request, ProxyStreamHandle channel) {
		String connHeader = request.getSingleHeaderValue(Http2HeaderName.CONNECTION);
		boolean close = false;
		if(!"keep-alive".equals(connHeader)) {
			close = true;
		} else
			close = false;
		
		if(close)
			channel.closeIfNeeded();
		
		return null;
	}

}
