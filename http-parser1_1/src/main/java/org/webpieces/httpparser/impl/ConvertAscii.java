package org.webpieces.httpparser.impl;

import java.util.HashMap;
import java.util.Map;

public class ConvertAscii {

	private Map<Integer, String> lookupTable = new HashMap<>();
	
	public ConvertAscii() {
		lookupTable.put(0, "[NUL]");
		lookupTable.put(1, "[SOH]");
		lookupTable.put(2, "[STX]");
		lookupTable.put(3, "[ETX]");
		lookupTable.put(4, "[EOT]");
		lookupTable.put(5, "[ENQ]");
		lookupTable.put(6, "[ACK]");
		lookupTable.put(7, "[BEL]");
		lookupTable.put(8, "[BS]");
		lookupTable.put(9, "\\t\t");
		lookupTable.put(10, "\\n\n");
		lookupTable.put(11, "\\vt    ");
		lookupTable.put(13, "\\r\r");
		lookupTable.put(32, "\\s ");

		
	}

	public boolean isCarriageReturn(byte byteVal) {
		int asciiInt = byteVal & 0xFF;
		if(asciiInt == 13) 
			return true;
		return false;
	}

	public boolean isLineFeed(byte byteVal) {
		int asciiInt = byteVal & 0xFF;
		if(asciiInt == 10) 
			return true;
		return false;
	}
	
	public String convertToReadableForm(byte[] msg) {
		return convertToReadableForm(msg, 0, msg.length);
	}
	
	//TODO: I think this is wrong for characters where byte is negative?  
	//Basically values between 128 to 255 part of an unsigned byte
	public String convertToReadableForm(byte[] msg, int offset, int length) {
		//let's walk two at a time so
		StringBuilder builder = new StringBuilder();
		int i = offset;
		int highMark = offset+length;
		for(; i < highMark-1; i++) {
			byte firstByte = msg[i];
			byte secondByte = msg[i+1];
			int asciiInt = firstByte & 0xFF;
			if(asciiInt >= 33) {
				appendDisplayableByte(builder, firstByte);
				continue;
			}
			
			boolean firstIsCarriageReturn = isCarriageReturn(firstByte);
			boolean secondIsLineFeed = isLineFeed(secondByte);
			if(firstIsCarriageReturn && secondIsLineFeed) {
				builder.append("\\r\\n\r\n");
				i++; //move to skip the line feed we just processed
			} else {
				appendInvisibleByte(builder, asciiInt);
			}
		}
		
		if(i < highMark) {
			byte lastByte = msg[i];
			int unsignedByte = lastByte & 0xFF;
			if(unsignedByte >= 33)
				appendDisplayableByte(builder, lastByte);
			else
				appendInvisibleByte(builder, unsignedByte);
		}
		
		return builder.toString();
	}

	private void appendInvisibleByte(StringBuilder builder, int asciiInt) {
		String printableForm = lookupTable.get(asciiInt);
		if(printableForm == null)
			throw new UnsupportedOperationException("not supported ascii yet int="+asciiInt);
		builder.append(printableForm);
	}

	private void appendDisplayableByte(StringBuilder builder, byte firstByte) {
		char c = (char)firstByte;
		builder.append(c);
	}
}
