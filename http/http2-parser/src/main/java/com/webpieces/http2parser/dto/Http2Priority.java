package com.webpieces.http2parser.dto;

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
	private boolean streamDependencyIsExclusive = false; //1 bit
	private int streamDependency = 0x0; //31 bits
	private byte weight = 0x0; //8

	public boolean isStreamDependencyIsExclusive() {
		return streamDependencyIsExclusive;
	}

	public void setStreamDependencyIsExclusive() {
		this.streamDependencyIsExclusive = true;
	}

	public int getStreamDependency() {
		return streamDependency;
	}

	public void setStreamDependency(int streamDependency) {
		this.streamDependency = streamDependency & 0x7FFFFFFF;
	}

	public byte getWeight() {
		return weight;
	}

	public void setWeight(byte weight) {
		this.weight = weight;
	}

	protected DataWrapper getPayloadDataWrapper() {
		ByteBuffer payload = ByteBuffer.allocate(5);
		payload.putInt(streamDependency);
		if(streamDependencyIsExclusive) payload.put(0, (byte) (payload.get(0) | 0x80));
		payload.put(weight);
		payload.flip();

		return new ByteBufferDataWrapper(payload);
	}

	protected void setFromPayload(DataWrapper payload) {
		ByteBuffer payloadByteBuffer = ByteBuffer.wrap(payload.createByteArray());
		int firstInt = payloadByteBuffer.getInt();
		streamDependencyIsExclusive = firstInt >>> 31 == 0x1;
		streamDependency = firstInt & 0x7FFFFFFF;
		weight = payloadByteBuffer.get();
	}
	
}
