package com.webpieces.http2parser.impl;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.twitter.hpack.HeaderListener;
import com.webpieces.http2parser.api.Http2HeaderBlock;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Http2HeaderBlockImpl implements Http2HeaderBlock {
    static private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    private final List<Http2HeaderBlock.Header> headers = new ArrayList<>();

    public DataWrapper getDataWrapper() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // TODO: get max header table size from settings
        Encoder encoder = new Encoder(4096);
        for (Http2HeaderBlock.Header header : headers) {
            try {
                encoder.encodeHeader(
                        out,
                        header.header.getBytes(),
                        header.value.getBytes(),
                        false);
            } catch (IOException e) {
                // TODO: reraise appropriately
            }
        }
        return dataGen.wrapByteArray(out.toByteArray());
    }

    public Map<String, String> getMap() {
        Map<String, String> headerMap = new HashMap<>();
        for (Http2HeaderBlock.Header header : headers) {
            headerMap.put(header.header, header.value);
        }
        return headerMap;
    }

    public void setFromMap(Map<String, String> map) {
        headers.clear();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            headers.add(new Http2HeaderBlockImpl.Header(entry.getKey(), entry.getValue()));
        }
    }

    public void setFromDataWrapper(DataWrapper data) {
        headers.clear();
        byte[] bytes = data.createByteArray();
        // TODO: get maxs from settings
        Decoder decoder = new Decoder(4096, 4096);
        HeaderListener listener = new HeaderListener() {
            @Override
            public void addHeader(byte[] name, byte[] value, boolean sensitive) {
                headers.add(new Http2HeaderBlock.Header(new String(name), new String(value)));
            }
        };
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            decoder.decode(in, listener);
        } catch (IOException e) {
            // TODO: reraise appropriately here
        }
        decoder.endHeaderBlock();
    }
}
