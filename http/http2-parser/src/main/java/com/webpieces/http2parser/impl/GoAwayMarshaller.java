package com.webpieces.http2parser.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.Http2GoAway;

public class GoAwayMarshaller extends FrameMarshallerImpl {
    GoAwayMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool, dataGen);
    }

    @Override
    public byte marshalFlags(AbstractHttp2Frame frame) {
        return 0x0;
    }

    @Override
    public DataWrapper marshalPayload(AbstractHttp2Frame frame) {
        Http2GoAway castFrame = (Http2GoAway) frame;

        ByteBuffer prelude = bufferPool.nextBuffer(8);
        prelude.putInt(castFrame.getLastStreamId()).putInt(castFrame.getErrorCode().getCode());
        prelude.flip();

        return dataGen.chainDataWrappers(
                dataGen.wrapByteBuffer(prelude),
                castFrame.getDebugData()
        );
    }

    @Override
    public void unmarshalFlagsAndPayload(AbstractHttp2Frame frame, byte flagsByte, Optional<DataWrapper> maybePayload) {
        Http2GoAway castFrame = (Http2GoAway) frame;

        maybePayload.ifPresent(payload ->
                {
                    List<? extends DataWrapper> split = dataGen.split(payload, 8);
                    ByteBuffer preludeBytes = bufferPool.createWithDataWrapper(split.get(0));

                    castFrame.setLastStreamId(preludeBytes.getInt());
                    castFrame.setErrorCode(Http2ErrorCode.fromInteger(preludeBytes.getInt()));

                    castFrame.setDebugData(split.get(1));

                    bufferPool.releaseBuffer(preludeBytes);
                }
        );
    }
}
