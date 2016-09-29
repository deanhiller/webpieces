package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2WindowUpdate;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import java.nio.ByteBuffer;

public class WindowUpdateMarshaller extends FrameMarshallerImpl {

    WindowUpdateMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    public byte getFlagsByte(Http2Frame frame) {
        return 0x0;
    }

    public DataWrapper getPayloadDataWrapper(Http2Frame frame) {
        Http2WindowUpdate castFrame = (Http2WindowUpdate) frame;

        ByteBuffer payload = bufferPool.nextBuffer(4).putInt(castFrame.getWindowSizeIncrement());
        payload.flip();

        return dataGen.wrapByteBuffer(payload);
    }

}
