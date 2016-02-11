package org.playorm.nio.api.handlers;

import java.nio.ByteBuffer;

public interface DataChunk {

	/**
	 * IMPORTANT: After reading ALL the data from ByteBuffer, and potentially after writing something
	 * upstream to another node, you MUST call setProcessed or we will not read any more data in 
	 * from the socket.  Why? Because this enables tcp flow control to take affect so the downstream server is slowed
	 * down and can't write to the socket anymore until you have processed your data.  It is a very clean way of
	 * slowly degrading and forcing clients to slowdown while you catchup.
	 * 
	 * NOTE: You should also call releaseBuffer when done reading the buffer.
	 * 
	 * @return
	 */
	public ByteBuffer getData();
	
	/**
	 * 
	 */
	public void setProcessed(String namedByteConsumerForLogs);

}
