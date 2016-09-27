package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.dto.Http2Frame;
import org.webpieces.data.api.DataWrapper;
import java.util.List;

class ParserResultImpl implements ParserResult {
    final private List<Http2Frame> frames;
    final private DataWrapper leftOverData;

    ParserResultImpl(List<Http2Frame> frames, DataWrapper leftOverData) {
        this.frames = frames;
        this.leftOverData = leftOverData;
    }

    public List<Http2Frame> getParsedFrames() {
        return frames;
    }

    public DataWrapper getMoreData() {
        return leftOverData;
    }

    public boolean hasMoreData() {
        return leftOverData.getReadableSize() > 0;
    }

    public boolean hasParsedFrames() {
        return frames.size() > 0;
    }
}
