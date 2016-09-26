package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;
import java.util.List;

public class Http2Headers extends Http2Frame {
	public Http2FrameType getFrameType() {
		return Http2FrameType.HEADERS;
	}

	/* flags */
	private boolean endStream; /* 0x1 */
	private boolean endHeaders; /* 0x4 */
	private boolean padded; /* 0x8 */
	private boolean priority; /* 0x20 */
	public byte getFlagsByte() {
		byte value = 0x0;
		if(endStream) value |= 0x1;
		if(endHeaders) value |= 0x4;
		if(padded) value |= 0x8;
		if(priority) value |= 0x20;
		return value;
	}
	public void setFlags(byte flags) {
		endStream = (flags & 0x1) == 0x1;
		endHeaders = (flags & 0x4) == 0x4;
		padded = (flags & 0x8) == 0x8;
		priority = (flags & 0x20) == 0x20;
	}

	/* payload */
	private boolean streamDependencyIsExclusive; //1 bit
	private int streamDependency; //31 bits
	private byte weight; //8 bits
	private Http2HeaderBlock headerBlock;
	private byte[] padding;

	protected DataWrapper getPayloadDataWrapper() {
		ByteBuffer prelude = ByteBuffer.allocate(5);
		prelude.putInt(streamDependency);
		if(streamDependencyIsExclusive) prelude.put(0, (byte) (prelude.get(0) | 0x8));
		prelude.put((byte) weight);

		DataWrapper unpadded = dataGen.chainDataWrappers(
				new ByteBufferDataWrapper(prelude),
				headerBlock.getDataWrapper());
		if(!padded) {
			return unpadded;
		}
		else {
			return pad(padding, unpadded);
		}
	}

	protected void setPayload(DataWrapper payload) {
		List<? extends DataWrapper> split = dataGen.split(payload, 5);
		ByteBuffer prelude = ByteBuffer.wrap(split.get(0).createByteArray());
		int firstInt = prelude.getInt();
		streamDependencyIsExclusive = firstInt >> 31 == 0x1;
		streamDependency = firstInt & 0x7FFFFFFF;
		weight = prelude.get();

		if(padded) {
			byte padLength = split.get(1).readByteAt(0);
			List<? extends DataWrapper> split1 = dataGen.split(split.get(1), 1);
			List<? extends DataWrapper> split2 = dataGen.split(split1.get(1), payload.getReadableSize() - padLength);
			headerBlock = new Http2HeaderBlock(split2.get(0));
			padding = split2.get(1).createByteArray();
		} else {
			padding = new byte[0];
			headerBlock = new Http2HeaderBlock(split.get(1));
		}
	}
}
