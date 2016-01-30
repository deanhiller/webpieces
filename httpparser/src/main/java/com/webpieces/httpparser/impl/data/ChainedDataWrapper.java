package com.webpieces.httpparser.impl.data;

import java.nio.charset.Charset;

import com.webpieces.httpparser.api.DataWrapper;

public class ChainedDataWrapper implements DataWrapper {

	private DataWrapper wrapper1;
	private DataWrapper wrapper2;
	private int wrapper1Size;

	public ChainedDataWrapper(DataWrapper wrapper1, DataWrapper wrapper2) {
		this.wrapper1 = wrapper1;
		this.wrapper2 = wrapper2;
		this.wrapper1Size = wrapper1.getReadableSize();
	}
	
	@Override
	public int getReadableSize() {
		return wrapper1Size + wrapper2.getReadableSize();
	}

	@Override
	public byte readByteAt(int i) {
		if(i < wrapper1Size) {
			return wrapper1.readByteAt(i);
		}
		
		return wrapper2.readByteAt(i - wrapper1Size);
	}

	@Override
	public String createStringFrom(int offset, int length, Charset charSet) {
		//need to implement if offset + length is only in wrapper1, use wrapper1
		//if offset + length is only in wrapper2, use wrapper2
		//if offset + length spans both wrappers, use both
		if(offset + length <= wrapper1Size) {
			return wrapper1.createStringFrom(offset, length, charSet);
		} else if(offset >= wrapper1Size) {
			return wrapper2.createStringFrom(offset - wrapper1Size, length, charSet);
		}

		int lengthOfPart1 = wrapper1Size - offset;
		String part1 = wrapper1.createStringFrom(offset, lengthOfPart1, charSet);
		int lengthOfPart2 = length - lengthOfPart1;
		String part2 = wrapper2.createStringFrom(0, lengthOfPart2, charSet);
		return part1 + part2;
	}

	@Override
	public byte[] createByteArray() {
		byte[] part1 = wrapper1.createByteArray();
		byte[] part2 = wrapper2.createByteArray();
		
		byte[] copy = new byte[part1.length+part2.length];
		
		System.arraycopy(part1, 0, copy, 0, part1.length);
		System.arraycopy(part2, 0, copy, part1.length, part2.length);
		return copy;
	}

}
