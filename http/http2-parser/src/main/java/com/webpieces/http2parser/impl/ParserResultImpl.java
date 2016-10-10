package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.ParserResult;
import org.webpieces.data.api.DataWrapper;

import java.util.List;

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
