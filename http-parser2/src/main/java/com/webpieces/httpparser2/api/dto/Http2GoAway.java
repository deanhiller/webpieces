package com.webpieces.httpparser2.api.dto;

import org.webpieces.data.api.DataWrapper;

public class Http2GoAway {

	private int lastStreamId;
	private long errorCode;
	private DataWrapper debugData;
}
