package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

import java.util.LinkedList;
import java.util.List;

public interface HasHeaders {
    class Header {
        public Header(String header, String value) {
            this.header = header;
            this.value = value;
        }

        public String header;
        public String value;
    }

    LinkedList<Header> getHeaders();
    void setHeaders(LinkedList<Header> headers);

    boolean isEndHeaders();

    void setEndHeaders(boolean endHeaders);

    DataWrapper getSerializedHeaders();
    void setSerializedHeaders(DataWrapper serialized);
}
