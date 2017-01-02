package org.webpieces.http2client.api.dto;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2engine.api.dto.Http2Headers;

public class Http2Response extends Http2Message {

	public Http2Response(Http2Headers headers, DataWrapper fullData, Http2Headers trailingHeaders) {
		super(headers, fullData, trailingHeaders);
	}

}
