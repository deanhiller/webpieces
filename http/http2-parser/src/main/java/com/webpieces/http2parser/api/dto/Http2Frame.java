package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;

public abstract class Http2Frame {
	protected DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	//24 bits unsigned length
	public abstract Http2FrameType getFrameType(); //8bits
	//1bit reserved
	private int streamId; //31 bits unsigned

	public DataWrapper getDataWrapper() {
		ByteBuffer header = ByteBuffer.allocate(9);
		DataWrapper payload = getPayloadDataWrapper();

		int length = payload.getReadableSize();
        header.put((byte) (length >> 16));
        header.putShort((short) length);
        
        header.put(getFrameTypeByte());
		header.put(getFlagsByte());
        // 1 bit reserved, clear MSB of streamId first;
        header.putInt(streamId & 0x7FFFFFFF);

		return dataGen.chainDataWrappers(new ByteBufferDataWrapper(header), payload);
	}

	public byte[] getBytes() {
		return getDataWrapper().createByteArray();
	}

	protected byte getFrameTypeByte() {
		return getFrameType().getId();
	}

	protected abstract byte getFlagsByte();
	protected abstract void setFlags(byte flag);

	abstract protected DataWrapper getPayloadDataWrapper();

	protected DataWrapper pad(byte[] padding, DataWrapper data) {
		byte[] length = { (byte) padding.length };
		DataWrapper lengthDW = dataGen.wrapByteArray(length);
		DataWrapper paddingDW = dataGen.wrapByteArray(padding);
		return dataGen.chainDataWrappers(dataGen.chainDataWrappers(lengthDW, data), paddingDW);
	}
}
