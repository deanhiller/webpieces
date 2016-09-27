package com.webpieces.http2parser.dto;

import org.webpieces.data.api.DataWrapper;

import java.util.List;

public class Http2Data extends Http2Frame {
	public Http2FrameType getFrameType() {
		return Http2FrameType.DATA;
	}

	/* flags */
	private boolean endStream = false; /* 0x1 */
	private boolean padded = false;    /* 0x8 */
	protected byte getFlagsByte() {
		byte value = (byte) 0x0;
		if(endStream) value |= 0x1;
		if(padded) value |= 0x8;
		return value;
	}

	protected void setFlags(byte flags) {
		endStream = (flags & 0x1) == 0x1;
		padded = (flags & 0x8) == 0x8;
	}


	/* payload */
	private DataWrapper data = null;
	private byte[] padding = null;

	public DataWrapper getData() {
		return data;
	}

	public void setData(DataWrapper data) {
		this.data = data;
	}

	public void setPadding(byte[] padding) {
		this.padding = padding;
		this.padded = true;
	}

	public boolean isEndStream() {
		return endStream;
	}

	public void setEndStream() {
		this.endStream = true;
	}

	protected DataWrapper getPayloadDataWrapper() {
		if(!padded) {
			return data;
		} else {
			return pad(padding, data);
		}
	}

	protected void setPayload(DataWrapper payload) {
		if(padded) {
			byte padLength = payload.readByteAt(0);
			List<? extends DataWrapper> split = dataGen.split(payload, 1);

			List<? extends DataWrapper> split2 = dataGen.split(split.get(1), split.get(1).getReadableSize() - padLength);
			setData(split2.get(0));
			setPadding(split2.get(1).createByteArray());
		} else {
			setData(payload);
		}
	}
}
