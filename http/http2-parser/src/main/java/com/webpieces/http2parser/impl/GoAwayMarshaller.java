package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2GoAway;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import java.nio.ByteBuffer;

public class GoAwayMarshaller extends FrameMarshallerImpl {
    GoAwayMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    public byte getFlagsByte(Http2Frame frame) {
        return 0x0;
    }

    public DataWrapper getPayloadDataWrapper(Http2Frame frame) {
        Http2GoAway castFrame = (Http2GoAway) frame;

        ByteBuffer prelude = bufferPool.nextBuffer(8);
        prelude.putInt(castFrame.getLastStreamId()).putInt(castFrame.getErrorCode().getCode());
        prelude.flip();

        return dataGen.chainDataWrappers(
                dataGen.wrapByteBuffer(prelude),
                castFrame.getDebugData()
        );
    }
}
