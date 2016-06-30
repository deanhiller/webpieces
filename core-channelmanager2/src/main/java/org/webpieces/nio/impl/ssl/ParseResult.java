package org.webpieces.nio.impl.ssl;

import java.nio.ByteBuffer;
import java.util.List;

public class ParseResult {

	private ByteBuffer buffer;
	private List<String> names;

	public ParseResult(ByteBuffer buffer, List<String> names) {
		this.buffer = buffer;
		this.names = names;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public List<String> getNames() {
		return names;
	}
	
}
