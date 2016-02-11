package org.playorm.nio.impl.cm.basic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.libs.BufferHelper;


/** 
 * @author dhiller
 */
public class BufferHelperImpl implements BufferHelper {

//	private static final int NUM_BYTES_IN_CHAR = Character.SIZE/8;
	private static final Logger log = Logger.getLogger(BufferHelperImpl.class.getName());
	
	public BufferHelperImpl() {
	}	
	
	/**
	 * CharBuffer absolutely sucks!!!  I filed one bug where it doesn't move the position
	 * of the original Buffer so you have to calculate the position...unfortunately, CharBuffer
	 * assumes 2 bytes per character unlike most of the character handling in java which has
	 * a specific charset encoding which may be just the systems default....I use
	 * the default encoding here using the io apis instead of the new io ones.  I should file
	 * another MR that CharBuffer does not have an encoding choice like much of old io Readers
	 * and Writers have.
	 * 
	 * @see org.playorm.nio.api.libs.BufferHelper#putString(java.nio.ByteBuffer, java.lang.String)
	 */
	public void putString(ByteBuffer b, String fullString) {
		if(b == null)
			throw new IllegalArgumentException("Cannot pass in a null buffer");
		else if(fullString == null)
			throw new IllegalArgumentException("Cannot pass in a null string");
		byte[] encodedString;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(out);
			writer.write(fullString);
			writer.flush();
			encodedString = out.toByteArray();
		} catch(IOException e) {
			throw new RuntimeException("Should never happen", e);
		}

		if (log.isLoggable(Level.FINEST)) {
			log.finest("1byteBuf pos="+b.position()+"  lim="+b.limit()+"  remain="+b.remaining());
		}
		b.put(encodedString);
		
		if (log.isLoggable(Level.FINEST)) {
			log.finest("1byteBuf pos="+b.position()+"  lim="+b.limit()+"  remain="+b.remaining());
		}		
	}
	
	/**
	 */
	public String readString(ByteBuffer b, int numBytesToRead) {
		byte[] buffer = new byte[numBytesToRead];
		b.get(buffer);
		String s = new String(buffer);
		return s;
	}
	
	/**
	 * @see org.playorm.nio.api.libs.BufferHelper#doneFillingBuffer(java.nio.ByteBuffer)
	 */
	public void doneFillingBuffer(ByteBuffer b) {
		b.flip();
		
		if (log.isLoggable(Level.FINEST)) {
			log.finest("1byteBuf pos="+b.position()+"  lim="+b.limit()+"  remain="+b.remaining());
		}

//		String s = "";
//		for(int i = b.position(); i < b.limit()-1; i++) {
//			char c = b.getChar(i); 
//			log.info("i="+i+" c='"+c+"'");
//			s += c;
//		}
//		
//		log.info("buffer='"+s+"'");
	}

	
	/**
	 * @see org.playorm.nio.api.libs.BufferHelper#eraseBuffer(java.nio.ByteBuffer)
	 */
	public void eraseBuffer(ByteBuffer b) {
		b.clear();
	}


	/**
	 * @see org.playorm.nio.api.libs.BufferHelper#processForPacket(java.nio.ByteBuffer, java.nio.ByteBuffer)
	 */
	public boolean processForPacket(ByteBuffer from, ByteBuffer dest) {
	
		int remain = from.remaining();
		int cacheSpace = dest.remaining();
		int sizeToCopy = cacheSpace;
		
		if(remain <= 0)
			return false;
		
		//reduce the size to copy.  The from buffer does not
		//have enough data to put the whole packet together
		if(remain < cacheSpace)
			sizeToCopy = remain;
		
		byte[] dst = dest.array();
		
		if(sizeToCopy > 0) {
			from.get(dst, dest.position(), sizeToCopy);
			int curPos = dest.position();
			dest.position(curPos+sizeToCopy);
		}
		
		if(dest.remaining() <= 0) {
			return true;
		} 
		
		return false;
	}
}
