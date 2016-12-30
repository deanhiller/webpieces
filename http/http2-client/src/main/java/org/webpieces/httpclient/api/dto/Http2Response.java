package org.webpieces.httpclient.api.dto;

import org.webpieces.data.api.DataWrapper;

public class Http2Response extends Http2Message {

	public Http2Response(Http2Headers headers, DataWrapper fullData, Http2Headers trailingHeaders) {
		super(headers, fullData, trailingHeaders);
	}

}
