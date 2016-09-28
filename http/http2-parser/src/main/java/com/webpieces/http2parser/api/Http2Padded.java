package com.webpieces.http2parser.api;

import org.webpieces.data.api.DataWrapper;

import static com.webpieces.http2parser.api.Http2FrameUtil.dataGen;

public interface Http2Padded {
    void setPadding(byte[] padding);

    static DataWrapper pad(byte[] padding, DataWrapper data) {
        byte[] length = { (byte) padding.length };
        DataWrapper lengthDW = dataGen.wrapByteArray(length);
        DataWrapper paddingDW = dataGen.wrapByteArray(padding);
        return dataGen.chainDataWrappers(dataGen.chainDataWrappers(lengthDW, data), paddingDW);
    }
}
