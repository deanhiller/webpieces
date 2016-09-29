package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.FrameMarshaller;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapperGenerator;

public abstract class FrameMarshallerImpl implements FrameMarshaller {
    protected BufferPool bufferPool;
    protected DataWrapperGenerator dataGen;

    FrameMarshallerImpl(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        this.bufferPool = bufferPool;
        this.dataGen = dataGen;
    }
}
