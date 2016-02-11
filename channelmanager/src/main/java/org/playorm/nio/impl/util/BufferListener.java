package org.playorm.nio.impl.util;

import java.nio.ByteBuffer;

public interface BufferListener {

	void releaseBuffer(ByteBuffer data);

}
