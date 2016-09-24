package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

public abstract class Http2Frame {
	protected DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	//24 bits unsigned length
	public abstract Http2FrameType getFrameType(); //8bits
	//1bit reserved
	private int streamId; //31 bits unsigned

	public DataWrapper getDataWrapper() {
		byte[] header = new byte[9];
		DataWrapper payload = getPayloadDataWrapper();

		int length = payload.getReadableSize();
		header[0] = (byte) (length >> 16);
		header[1] = (byte) (length >> 8);
		header[2] = (byte) length;
		header[3] = getFrameTypeByte();
		header[4] = getFlagsByte();
		header[5] = (byte) (streamId >> 24);
		header[6] = (byte) (streamId >> 16);
		header[7] = (byte) streamId;

		DataWrapper headerDW = dataGen.wrapByteArray(header);
		return dataGen.chainDataWrappers(headerDW, payload);
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
