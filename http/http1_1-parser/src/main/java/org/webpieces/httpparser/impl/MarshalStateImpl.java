package org.webpieces.httpparser.impl;

import org.webpieces.httpparser.api.MarshalState;

public class MarshalStateImpl implements MarshalState {

	private Integer parsingDataSize;
	private int totalBytesRead;

	public Integer getParsingDataSize() {
		return parsingDataSize;
	}

	public void setParsingDataSize(Integer parsingDataSize) {
		this.parsingDataSize = parsingDataSize;
	}

	public void addMoreBytes(int readableSize) {
		totalBytesRead += readableSize;
	}

	public int getTotalRead() {
		return totalBytesRead;
	}

	public void resetDataReading() {
		totalBytesRead = 0;
		parsingDataSize = null;
	}

}
