package org.playorm.nio.api.libs;

import java.io.IOException;
import java.nio.ByteBuffer;


public interface PacketProcessor {
	
	public ByteBuffer processOutgoing(ByteBuffer b);
	
	/**
	 * processIncoming reads from the ByteBuffer
	 * @param  true if we passed data to downstream listener, false if not
	 * @return 
	 * @throws IOException 
	 */
	public boolean incomingData(ByteBuffer b, Object passthrough) throws IOException;
	
	public void setPacketListener(PacketListener l);
	
}
