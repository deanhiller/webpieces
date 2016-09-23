package com.webpieces.http2parser.api.dto;

import java.util.List;

class Http2HeaderBlock {
    class Header {
        public String header;
        public String value;
    }

    private List<Header> headers;
}
