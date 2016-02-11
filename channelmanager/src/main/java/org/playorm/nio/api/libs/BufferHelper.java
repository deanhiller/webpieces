package org.playorm.nio.api.libs;

import java.nio.ByteBuffer;

/** 
 * @author dhiller
 */
public interface BufferHelper {

	/**
	 * @param b
	 * @param fullString
	 */
	void putString(ByteBuffer b, String fullString);

	/**
	 * @param b
	 */
	String readString(ByteBuffer b, int numBytesToRead);
	
	/**
	 * @param b
	 */
	void doneFillingBuffer(ByteBuffer b);

	/**
	 * @param b
	 */
	void eraseBuffer(ByteBuffer b);
	
	/**
	 * This method can be called repeatedly and will return true when the dest buffer
	 * finally contains a single packet of size equal to dest.limit().  The dest.limit(int)
	 * should be set before calling this function and should not be changed while the
	 * packet is being processed!!!!!!
	 * 
	 * The from buffer may have leftover data in it.  It should be fed in with a new or
	 * cleared destination.
	 * 
	 * @param from The ByteBuffer to copy from
	 * @param dest The ByteBuffer to copy to (Make sure you set the limit properly on the dest as
	 * you may only want the limit to be 200 bytes instead of what the ByteBuffer can actually
	 * contain.
	 * @return true when the dest buffer finally contains a single packet of size equal to dest.limit()
	 */
	public boolean processForPacket(ByteBuffer from, ByteBuffer dest);	

}
