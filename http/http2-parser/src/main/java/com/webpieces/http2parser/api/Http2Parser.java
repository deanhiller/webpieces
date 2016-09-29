package com.webpieces.http2parser.api;

import com.webpieces.http2parser.api.dto.Http2Frame;
import org.webpieces.data.api.DataWrapper;

public interface Http2Parser {
    Http2Frame unmarshal(DataWrapper data);

    DataWrapper marshal(Http2Frame frame);

    ParserResult parse(DataWrapper oldData, DataWrapper newData);
}
