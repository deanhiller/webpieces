package com.webpieces.http2parser.impl;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.twitter.hpack.HeaderListener;
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

public class Http2HeaderBlock {
    static private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    static class Header {
        Header(String header, String value) {
            this.header = header;
            this.value = value;
        }

        String header;
        String value;
    }

    private final List<Header> headers;

    Http2HeaderBlock(List<Header> headers) {
        this.headers = headers;
    }

    DataWrapper getDataWrapper() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // TODO: get max header table size from settings
        Encoder encoder = new Encoder(4096);
        for(Header header: headers) {
            try {
                encoder.encodeHeader(
                        out,
                        header.header.getBytes(),
                        header.value.getBytes(),
                        false);
            } catch(IOException e) {
                // TODO: reraise appropriately
            }
        }
        return dataGen.wrapByteArray(out.toByteArray());
    }

    Map<String, String> toMap() {
        Map<String, String> headerMap = new HashMap<>();
        for(Header header: headers) {
            headerMap.put(header.header, header.value);
        }
        return headerMap;
    }

    Http2HeaderBlock(DataWrapper data) {
        headers = new ArrayList<>();
        setFromDataWrapper(data);
    }

    private void setFromDataWrapper(DataWrapper data) {
        byte[] bytes = data.createByteArray();
        // TODO: get maxs from settings
        Decoder decoder = new Decoder(4096, 4096);
        HeaderListener listener = new HeaderListener() {
            @Override
            public void addHeader(byte[] name, byte[] value, boolean sensitive) {
                headers.add(new Header(new String(name), new String(value)));
            }
        };
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            decoder.decode(in, listener);
        } catch(IOException e) {
            // TODO: reraise appropriately here
        }
        decoder.endHeaderBlock();
    }
}
