package com.webpieces.http2parser.api;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public interface Http2StatefulParser {

	DataWrapper marshalToByteBuffer(Http2Frame request);
	
	List<Http2Frame> parse(DataWrapper moreData, long maxFrameSize);
	
}
