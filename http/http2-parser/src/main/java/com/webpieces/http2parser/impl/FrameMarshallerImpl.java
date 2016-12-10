package com.webpieces.http2parser.impl;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.FrameMarshaller;

abstract class FrameMarshallerImpl implements FrameMarshaller {
    BufferPool bufferPool;
    DataWrapperGenerator dataGen;

    FrameMarshallerImpl(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        this.bufferPool = bufferPool;
        this.dataGen = dataGen;
    }
}
