package com.webpieces.http2parser.api.dto;

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
import java.util.List;

class Http2HeaderBlock {
    private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    class Header {
        public Header(String header, String value) {
            this.header = header;
            this.value = value;
        }

        public String header;
        public String value;
    }

    private List<Header> headers;

    protected DataWrapper getDataWrapper() {
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

            }
        }
        return dataGen.wrapByteArray(out.toByteArray());
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
                headers.add(new Header(name.toString(), value.toString()));
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
