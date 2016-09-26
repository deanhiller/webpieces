package dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;

class Http2Priority extends Http2Frame {
	public Http2FrameType getFrameType() {
		return Http2FrameType.PRIORITY;
	}

	/* flags */
	public byte getFlagsByte() {
		return 0x0;
	}
	public void setFlags(byte flags) {}

	/* payload */
	private boolean streamDependencyIsExclusive; //1 bit
	private int streamDependency; //31 bits
	private byte weight; //8

	protected DataWrapper getPayloadDataWrapper() {
		ByteBuffer payload = ByteBuffer.allocate(5);
		payload.putInt(streamDependency);
		if(streamDependencyIsExclusive) payload.put(0, (byte) (payload.get(0) | 0x8));
		payload.put(weight);
		payload.flip();

		return new ByteBufferDataWrapper(payload);
	}

	protected void setPayload(DataWrapper payload) {
		ByteBuffer payloadByteBuffer = ByteBuffer.wrap(payload.createByteArray());
		int firstInt = payloadByteBuffer.getInt();
		streamDependencyIsExclusive = firstInt >> 31 == 0x1;
		streamDependency = payloadByteBuffer.getInt() & 0x7FFFFFFF;
		weight = payloadByteBuffer.get();
	}
	
}
