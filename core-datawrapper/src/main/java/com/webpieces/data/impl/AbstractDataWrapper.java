package com.webpieces.data.impl;

import com.webpieces.data.api.DataWrapper;

public abstract class AbstractDataWrapper implements DataWrapper {

	public int getNumLayers() {
		return 1;
	}
}
