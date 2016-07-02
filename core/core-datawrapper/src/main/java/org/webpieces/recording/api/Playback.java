package org.webpieces.recording.api;

import java.nio.ByteBuffer;

public interface Playback {

	/**
	 * returns null when all packets are exhausted
	 * @return
	 */
	public ByteBuffer getNextPacket();
	
}
