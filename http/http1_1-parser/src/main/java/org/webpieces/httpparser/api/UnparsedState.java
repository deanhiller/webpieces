package org.webpieces.httpparser.api;

public class UnparsedState {

	private ParsingState currentlyParsing;
	private int currentUnparsedSize;

	public UnparsedState(ParsingState state, int readableSize) {
		this.currentlyParsing = state;
		this.currentUnparsedSize = readableSize;
	}

	public ParsingState getCurrentlyParsing() {
		return currentlyParsing;
	}

	public int getCurrentUnparsedSize() {
		return currentUnparsedSize;
	}
	
}
