package com.webpieces.http2parser.api;

import org.webpieces.data.api.DataWrapper;

import java.util.List;

public interface ParserResult {
    boolean hasParsedFrames();
    List<Http2Frame> getParsedFrames();

    boolean hasMoreData();
    DataWrapper getMoreData();
}
