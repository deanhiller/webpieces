package org.webpieces.nio.api.channels;

import java.nio.ByteBuffer;

public interface FromSocket {

	/**
	 * The contract here is 
	 * 1. the byteBuffer will be ready for reading from.(ie. after the 
	 * ByteBuffer is filled, flip has already been called.)
	 * 2. you MUST read all the data(ie. buffer.position() has to equal buffer.limit()
	 * 
	 * Realize with the threaded implementation of ChannelManager, you may receive some
	 * data after the far end has closed(hmmm, wonder if we can do anything about that)
	 * 
	 * @param channel
	 * @param b
	 */
	public void incomingData(Channel channel, ByteBuffer b);
	
	public void farEndClosed(Channel channel);

	/**
	 * This is called in the case of udp when the packet was not read by other
	 * end either because it can't get there or because the other end is not
	 * listening, etc. etc.
	 * 
	 * @param channel
	 * @param data TODO
	 * @param e
	 */
	public void failure(Channel channel, ByteBuffer data, Exception e);
	
	/**
	 * There is limits on writing out asynchronously in any nio client.  You can only back
	 * up so much before you blow your RAM.  Therefore, there is a setting 
	 * TCPChannel.setMaxBytesWriteBackupSize which if reached will call applyBackPressure
	 * 
	 * Once 'all' backed up writes are written, releaseBackpressure will be called.  This
	 * is to avoid 'jitter' which can be a big performance penalty where we apply/release
	 * apply/release
	 * 
	 * @param channel
	 */
	public void applyBackPressure(Channel channel);
	
	public void releaseBackPressure(Channel channel);
	
}
