package com.webpieces.http2parser.api;

import com.webpieces.http2parser.impl.HeaderBlockImpl;
import org.webpieces.data.api.DataWrapper;

import java.util.Map;

public interface HeaderBlock {
    class Header {
        public Header(String header, String value) {
            this.header = header;
            this.value = value;
        }

        public String header;
        public String value;
    }

    DataWrapper serialize();

    Map<String, String> getMap();

    void setFromMap(Map<String, String> map);

    void deserialize(DataWrapper data);

}
