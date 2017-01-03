package com.webpieces.http2parser.api;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public interface ParserResult {

    List<Http2Frame> getParsedFrames();

    DataWrapper getMoreData();
}
