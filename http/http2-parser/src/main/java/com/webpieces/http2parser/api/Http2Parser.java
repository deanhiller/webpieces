package com.webpieces.http2parser.api;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.webpieces.http2parser.api.dto.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.Http2FrameType;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;

public interface Http2Parser {
	
    DataWrapper prepareToParse();

    AbstractHttp2Frame unmarshal(DataWrapper data);

    DataWrapper marshal(AbstractHttp2Frame frame);
    DataWrapper marshal(List<AbstractHttp2Frame> frames);

    FrameMarshaller getMarshaller(Class<? extends AbstractHttp2Frame> frameClass);

    // TODO: add a marshal to bytebuffer so we can use our bufferpool
    int getFrameLength(AbstractHttp2Frame frame);

    ParserResult parse(DataWrapper oldData, DataWrapper newData, Decoder decoder, List<Http2Setting> settings);

    DataWrapper serializeHeaders(LinkedList<Http2Header> headers, Encoder encoder, ByteArrayOutputStream out);
    List<AbstractHttp2Frame> createHeaderFrames(LinkedList<Http2Header> headers,
                                        Http2FrameType frameType,
                                        int streamId,
                                        Http2SettingsMap remoteSettings,
                                        Encoder encoder,
                                        ByteArrayOutputStream out);

    LinkedList<Http2Header> deserializeHeaders(DataWrapper headerPayload, Decoder decoder);
}
