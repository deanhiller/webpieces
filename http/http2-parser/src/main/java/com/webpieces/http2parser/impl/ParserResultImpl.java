package com.webpieces.http2parser.impl;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public class ParserResultImpl implements ParserResult {
    private List<Http2Frame> frames = new ArrayList<>();
    private DataWrapper leftOverData = DataWrapperGeneratorFactory.EMPTY;

    @Override
    public List<Http2Frame> getParsedFrames() {
        return frames;
    }

    @Override
    public DataWrapper getMoreData() {
        return leftOverData;
    }

	public void setParsedFrames(List<Http2Frame> frames2) {
		this.frames = frames2;
	}

	public void setLeftOverData(DataWrapper leftOverData) {
		this.leftOverData = leftOverData;
	}

}
