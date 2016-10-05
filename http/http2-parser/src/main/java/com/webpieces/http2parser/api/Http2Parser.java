package com.webpieces.http2parser.api;

import com.webpieces.http2parser.api.dto.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2FrameType;
import com.webpieces.http2parser.api.dto.Http2Settings;
import org.webpieces.data.api.DataWrapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface Http2Parser {
    DataWrapper prepareToParse();

    Http2Frame unmarshal(DataWrapper data);

    DataWrapper marshal(Http2Frame frame);
    DataWrapper marshal(List<Http2Frame> frames);

    ParserResult parse(DataWrapper oldData, DataWrapper newData);

    DataWrapper serializeHeaders(LinkedList<HasHeaderFragment.Header> headers);
    List<Http2Frame> createHeaderFrames(LinkedList<HasHeaderFragment.Header> headers,
                                        Http2FrameType frameType,
                                        int streamId,
                                        Map<Http2Settings.Parameter, Integer> remoteSettings);

    LinkedList<HasHeaderFragment.Header> deserializeHeaders(DataWrapper headerPayload);
}
