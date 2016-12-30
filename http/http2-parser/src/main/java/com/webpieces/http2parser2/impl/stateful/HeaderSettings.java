package com.webpieces.http2parser2.impl.stateful;

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
