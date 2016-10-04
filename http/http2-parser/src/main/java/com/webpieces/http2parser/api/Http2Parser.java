package com.webpieces.http2parser.api;

import com.webpieces.http2parser.api.dto.HasHeaders;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2FrameType;
import com.webpieces.http2parser.api.dto.Http2Settings;
import org.webpieces.data.api.DataWrapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.IntBinaryOperator;

public interface Http2Parser {
    DataWrapper prepareToParse();

    Http2Frame unmarshal(DataWrapper data);

    DataWrapper marshal(Http2Frame frame);

    ParserResult parse(DataWrapper oldData, DataWrapper newData);

    DataWrapper serializedHeaders(LinkedList<HasHeaders.Header> headers);
    List<Http2Frame> createHeaderFrames(LinkedList<HasHeaders.Header> headers, Class<? extends HasHeaders> startingFrameType, int streamId);
    LinkedList<HasHeaders.Header> deserializeHeaders(DataWrapper headerPayload);
}
