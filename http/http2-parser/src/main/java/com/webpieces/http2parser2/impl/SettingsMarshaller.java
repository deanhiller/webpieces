package com.webpieces.http2parser2.impl;

import java.nio.ByteBuffer;
import java.util.Map;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.Http2SettingsMap;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2Settings;

public class SettingsMarshaller extends AbstractFrameMarshaller implements FrameMarshaller {

	SettingsMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
		super(bufferPool, dataGen);
	}

	@Override
	public DataWrapper marshal(Http2Frame frame) {
		Http2Settings castFrame = (Http2Settings) frame;

		byte flags = 0x0;
		if (castFrame.isAck())
			flags |= 0x1;

		DataWrapper dataPayload;
		if (castFrame.isAck() || castFrame.getSettings().size() == 0) {
			// If ack or empty settings
			dataPayload = dataGen.emptyWrapper();
		} else {
			Http2SettingsMap settings = castFrame.getSettings();
			ByteBuffer payload = bufferPool.nextBuffer(6 * settings.size());

			for (Map.Entry<Http2Settings.Parameter, Long> setting : settings.entrySet()) {
				short id = setting.getKey().getId();
				Long value = setting.getValue();
				payload.putShort(id).putInt(value.intValue());
			}
			payload.flip();

			dataPayload = dataGen.wrapByteBuffer(payload);
		}
		return super.createFrame(frame, flags, dataPayload);
	}

	@Override
	public Http2Frame unmarshal(Http2MementoImpl state, DataWrapper payload) {
		Http2Settings frame = new Http2Settings();
		super.fillInFrameHeader(state, frame);

		byte flags = state.getFrameHeaderData().getFlagsByte();
		frame.setAck((flags & 0x1) == 0x1);

		ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(payload);

		while (payloadByteBuffer.hasRemaining()) {
			frame.setSetting(Http2Settings.Parameter.fromId(payloadByteBuffer.getShort()),
					payloadByteBuffer.getInt() & 0xFFFFFFFFL);
		}

		bufferPool.releaseBuffer(payloadByteBuffer);

		return frame;
	}
}
