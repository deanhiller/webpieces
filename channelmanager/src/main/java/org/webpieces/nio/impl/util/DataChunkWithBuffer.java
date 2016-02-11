package org.webpieces.nio.impl.util;

import org.webpieces.nio.api.handlers.DataChunk;

public interface DataChunkWithBuffer extends DataChunk {

	public void releaseBuffer(String logInfo);
	
	public void setProcessedImpl();
	
}
