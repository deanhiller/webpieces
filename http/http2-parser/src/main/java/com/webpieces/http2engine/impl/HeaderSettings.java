package com.webpieces.http2engine.impl;

public class HeaderSettings {

	private int headerTableSize;
	private long maxFrameSize;

	public int getHeaderTableSize() {
		return headerTableSize;
	}

	public long getMaxFrameSize() {
		return maxFrameSize;
	}

}
