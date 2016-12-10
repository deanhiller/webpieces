package com.webpieces.http2parser.api;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.Http2Frame;

public interface ParserResult {
    boolean hasParsedFrames();

    List<Http2Frame> getParsedFrames();

    boolean hasMoreData();

    DataWrapper getMoreData();
}
