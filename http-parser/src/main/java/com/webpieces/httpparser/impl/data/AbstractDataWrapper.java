package com.webpieces.httpparser.impl.data;

import com.webpieces.httpparser.api.DataWrapper;

public abstract class AbstractDataWrapper implements DataWrapper {

	public int getNumLayers() {
		return 1;
	}
}
