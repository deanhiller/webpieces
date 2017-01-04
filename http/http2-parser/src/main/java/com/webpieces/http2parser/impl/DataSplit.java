package com.webpieces.http2parser.impl;

import org.webpieces.data.api.DataWrapper;

public class DataSplit {

	private DataWrapper payload;
	private DataWrapper padding;

	public DataSplit(DataWrapper payload, DataWrapper padding) {
		this.payload = payload;
		this.padding = padding;
	}

	public DataWrapper getPayload() {
		return payload;
	}

	public DataWrapper getPadding() {
		return padding;
	}
	
}
