package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

public class Http2GoAway {

	private int lastStreamId;
	private long errorCode;
	private DataWrapper debugData;
}
