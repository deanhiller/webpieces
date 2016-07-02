package org.webpieces.data.impl;

import org.webpieces.data.api.DataWrapper;

public abstract class AbstractDataWrapper implements DataWrapper {

	public int getNumLayers() {
		return 1;
	}
}
