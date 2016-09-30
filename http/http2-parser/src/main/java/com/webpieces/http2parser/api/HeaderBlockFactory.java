package com.webpieces.http2parser.api;

import com.webpieces.http2parser.impl.HeaderBlockImpl;

public class HeaderBlockFactory {
    public static HeaderBlock createHeaderBlock() {
        return new HeaderBlockImpl();
    }
}
