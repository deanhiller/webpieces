package com.webpieces.httpparser.impl;

import java.util.HashMap;
import java.util.Map;

public class ConvertAscii {

	private Map<Integer, String> lookupTable = new HashMap<>();
	
	public ConvertAscii() {
		lookupTable.put(9, "\\t\t");
		lookupTable.put(11, "\\vt    ");
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
	
	public void printBytes(byte[] msg) {
		//let's walk two at a time so
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < msg.length-1; i++) {
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
				String printableForm = lookupTable.get(asciiInt);
				builder.append(printableForm);
			}
		}
	}

	private void appendDisplayableByte(StringBuilder builder, byte firstByte) {
		char c = (char)firstByte;
		builder.append(c);
	}
}
