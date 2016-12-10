package com.webpieces.http2parser.api;

import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.webpieces.http2parser.impl.PaddingImpl;

public class PaddingFactory {
    static public Padding createPadding() {
        return new PaddingImpl(DataWrapperGeneratorFactory.createDataWrapperGenerator());
    }
}
