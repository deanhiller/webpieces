package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.FrameMarshaller;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapperGenerator;

abstract class FrameMarshallerImpl implements FrameMarshaller {
    BufferPool bufferPool;
    DataWrapperGenerator dataGen;

    FrameMarshallerImpl(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        this.bufferPool = bufferPool;
        this.dataGen = dataGen;
    }
}
