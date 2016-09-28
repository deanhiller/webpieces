package com.webpieces.http2parser.api;

import org.webpieces.data.api.DataWrapper;

import java.util.Map;

public interface Http2HeaderBlock {
    class Header {
        public Header(String header, String value) {
            this.header = header;
            this.value = value;
        }

        public String header;
        public String value;
    }

    DataWrapper getDataWrapper();

    Map<String, String> getMap();

    void setFromMap(Map<String, String> map);

    void setFromDataWrapper(DataWrapper data);
}
