package org.webpieces.data.impl;

import org.webpieces.data.api.DataWrapper;

public abstract class AbstractDataWrapper implements DataWrapper {

	public int getNumLayers() {
		return 1;
	}

	public byte[] readBytesAt(int i, int len) {
		byte[] bytes = new byte[len];
		for(int pos = i; pos < i + len; pos++) {
			bytes[pos - i] = readByteAt(pos);
		}
		return bytes;
	}
}
