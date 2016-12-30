package com.webpieces.http2parser.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.dto.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.Http2Settings;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;
import com.webpieces.http2parser2.impl.UnsignedData;

public class SettingsMarshaller extends FrameMarshallerImpl {

    SettingsMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    @Override
    public DataWrapper marshalPayload(AbstractHttp2Frame frame) {
        Http2Settings castFrame = (Http2Settings) frame;

        if (castFrame.isAck() || castFrame.getSettings().size() == 0) {
            // If ack or empty settings
            return dataGen.emptyWrapper();
        } else {
        	List<Http2Setting> settings = castFrame.getSettings();
            ByteBuffer payload = bufferPool.nextBuffer(6 * settings.size());

			for (Http2Setting setting : settings) {
				UnsignedData.putUnsignedShort(payload, setting.getId());
				UnsignedData.putUnsignedInt(payload, setting.getValue());
			}
            payload.flip();

            return dataGen.wrapByteBuffer(payload);
        }
    }

    @Override
    public byte marshalFlags(AbstractHttp2Frame frame) {
        Http2Settings castFrame = (Http2Settings) frame;

        byte value = 0x0;
        if (castFrame.isAck()) value |= 0x1;
        return value;
    }

    @Override
    public void unmarshalFlagsAndPayload(AbstractHttp2Frame frame, byte flags, Optional<DataWrapper> maybePayload) {
        Http2Settings castFrame = (Http2Settings) frame;

        castFrame.setAck((flags & 0x1) == 0x1);

        maybePayload.ifPresent(payload -> {
            ByteBuffer payloadByteBuffer = bufferPool.createWithDataWrapper(payload);

    		while (payloadByteBuffer.hasRemaining()) {
    			int id = UnsignedData.getUnsignedShort(payloadByteBuffer);
    			long value = UnsignedData.getUnsignedInt(payloadByteBuffer);
    			SettingsParameter key = SettingsParameter.lookup(id);
    			castFrame.addSetting(new Http2Setting(id, value));
    		}
    		
            bufferPool.releaseBuffer(payloadByteBuffer);
        });
    }
}
