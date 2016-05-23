package com.webpieces.httpparser2.api.dto;

import com.webpieces.data.api.DataWrapper;

public class Http2GoAway {

	private int lastStreamId;
	private long errorCode;
	private DataWrapper debugData;
}
