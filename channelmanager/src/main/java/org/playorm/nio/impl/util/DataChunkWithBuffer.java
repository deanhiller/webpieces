package org.playorm.nio.impl.util;

import org.playorm.nio.api.handlers.DataChunk;

public interface DataChunkWithBuffer extends DataChunk {

	public void releaseBuffer(String logInfo);
	
	public void setProcessedImpl();
	
}
