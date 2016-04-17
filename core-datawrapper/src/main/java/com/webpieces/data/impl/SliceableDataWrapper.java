package com.webpieces.data.impl;

import java.nio.ByteBuffer;

public abstract class SliceableDataWrapper extends AbstractDataWrapper {

	public abstract ByteBuffer getSlicedBuffer(int offset, int length);

}
