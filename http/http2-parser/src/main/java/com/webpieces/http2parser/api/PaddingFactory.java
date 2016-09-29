package com.webpieces.http2parser.api;

import com.webpieces.http2parser.impl.PaddingImpl;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

public class PaddingFactory {
    static public Padding createPadding() {
        return new PaddingImpl(DataWrapperGeneratorFactory.createDataWrapperGenerator());
    }
}
