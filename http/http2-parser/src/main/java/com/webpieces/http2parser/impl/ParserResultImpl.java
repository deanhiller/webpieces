package com.webpieces.http2parser.impl;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public class ParserResultImpl implements ParserResult {
    final private List<Http2Frame> frames;
    final private DataWrapper leftOverData;

    public ParserResultImpl(List<Http2Frame> frames, DataWrapper leftOverData) {
        this.frames = frames;
        this.leftOverData = leftOverData;
    }

    @Override
    public List<Http2Frame> getParsedFrames() {
        return frames;
    }

    @Override
    public DataWrapper getMoreData() {
        return leftOverData;
    }

    @Override
    public boolean hasMoreData() {
        return leftOverData.getReadableSize() > 0;
    }

    @Override
    public boolean hasParsedFrames() {
        return frames.size() > 0;
    }
}
