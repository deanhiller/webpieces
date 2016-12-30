package com.webpieces.http2parser2.impl;

import java.nio.ByteBuffer;
import java.util.List;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2Setting;
import com.webpieces.http2parser.api.dto.Http2Settings;
import com.webpieces.http2parser.api.dto.SettingsParameter;

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
			List<Http2Setting> settings = castFrame.getSettings();
			ByteBuffer payload = bufferPool.nextBuffer(6 * settings.size());

			for (Http2Setting setting : settings) {
				UnsignedData.putUnsignedShort(payload, setting.getId());
				UnsignedData.putUnsignedInt(payload, setting.getValue());
			}
			payload.flip();

			dataPayload = dataGen.wrapByteBuffer(payload);
		}
		return super.marshalFrame(frame, flags, dataPayload);
	}

	@Override
	public AbstractHttp2Frame unmarshal(Http2MementoImpl state, DataWrapper payload) {
		FrameHeaderData frameHeaderData = state.getFrameHeaderData();
		int payloadLength = frameHeaderData.getPayloadLength();
		int streamId = frameHeaderData.getStreamId();
        
		Http2Settings frame = new Http2Settings();
		super.unmarshalFrame(state, frame);

		byte flags = state.getFrameHeaderData().getFlagsByte();
		frame.setAck((flags & 0x1) == 0x1);

		if(frame.isAck()) {
	        if(payloadLength != 0) {
	            throw new ParseException(Http2ErrorCode.FRAME_SIZE_ERROR, streamId, false);
	        }
		} else if(payloadLength % 6 != 0) {
            throw new ParseException(Http2ErrorCode.FRAME_SIZE_ERROR, streamId, false);
        } else if(streamId != 0)
            throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, streamId, false);
        
		ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(payload);

		while (payloadByteBuffer.hasRemaining()) {
			int id = UnsignedData.getUnsignedShort(payloadByteBuffer);
			long value = UnsignedData.getUnsignedInt(payloadByteBuffer);
			SettingsParameter key = SettingsParameter.lookup(id);
			frame.addSetting(new Http2Setting(id, value));
			validate(key, value);
		}

		bufferPool.releaseBuffer(payloadByteBuffer);

		return frame;
	}

	private void validate(SettingsParameter key, long value) {
		if(key == null)
			return; //unknown setting
		
		switch(key) {
			case SETTINGS_ENABLE_PUSH:
				if(value != 0 && value != 1)
					throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR);
				break;
			case SETTINGS_INITIAL_WINDOW_SIZE:
				validateWindowSize(value);
				break;
			case SETTINGS_MAX_FRAME_SIZE:
				validateMaxFrameSize(value);
				break;
			case SETTINGS_HEADER_TABLE_SIZE:
			case SETTINGS_MAX_CONCURRENT_STREAMS:
			case SETTINGS_MAX_HEADER_LIST_SIZE:
				break;
			default:
				throw new IllegalArgumentException("case statement missing new setting="+key+" with value="+value);
		}
	}

	private void validateWindowSize(long value) {
        // 2^31 - 1 - max flow control window
		int min = 0;
		int max = 2147483647;
		
		if(value < min || value > max)
			throw new ParseException(Http2ErrorCode.FLOW_CONTROL_ERROR);
	}
	
	private void validateMaxFrameSize(long value) {
        // frame size must be between 16384 and 2^24 - 1
		int min = 16384;
		int max = 1677215;
		
		if(value < min || value > max)
			throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR);
	}
}