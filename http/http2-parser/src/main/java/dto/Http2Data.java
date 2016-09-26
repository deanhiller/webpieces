package dto;

import org.webpieces.data.api.DataWrapper;

import java.util.List;

public class Http2Data extends Http2Frame {
	public Http2FrameType getFrameType() {
		return Http2FrameType.DATA;
	}

	/* flags */
	private boolean endStream; /* 0x1 */
	private boolean padded;    /* 0x8 */
	protected byte getFlagsByte() {
		byte value = (byte) 0x0;
		if(endStream) value |= 0x1;
		if(padded) value |= 0x8;
		return value;
	}
	public void setFlags(byte flags) {
		endStream = (flags & 0x1) == 0x1;
		padded = (flags & 0x8) == 0x8;
	}


	/* payload */
	private DataWrapper data;
	private byte[] padding;

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
			List<? extends DataWrapper> split2 = dataGen.split(split.get(1), payload.getReadableSize() - padLength);
			data = split2.get(0);
			padding = split2.get(1).createByteArray();
		} else {
			padding = new byte[0];
			data = payload;
		}
	}
}
