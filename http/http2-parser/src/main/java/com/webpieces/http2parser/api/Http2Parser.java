package com.webpieces.http2parser.api;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.webpieces.http2parser.api.dto.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2FrameType;

public interface Http2Parser {
    DataWrapper prepareToParse();

    Http2Frame unmarshal(DataWrapper data);

    DataWrapper marshal(Http2Frame frame);
    DataWrapper marshal(List<Http2Frame> frames);

    FrameMarshaller getMarshaller(Class<? extends Http2Frame> frameClass);

    // TODO: add a marshal to bytebuffer so we can use our bufferpool
    int getFrameLength(Http2Frame frame);

    ParserResult parse(DataWrapper oldData, DataWrapper newData, Decoder decoder, Http2SettingsMap settings);

    DataWrapper serializeHeaders(LinkedList<HasHeaderFragment.Header> headers, Encoder encoder, ByteArrayOutputStream out);
    List<Http2Frame> createHeaderFrames(LinkedList<HasHeaderFragment.Header> headers,
                                        Http2FrameType frameType,
                                        int streamId,
                                        Http2SettingsMap remoteSettings,
                                        Encoder encoder,
                                        ByteArrayOutputStream out);

    LinkedList<HasHeaderFragment.Header> deserializeHeaders(DataWrapper headerPayload, Decoder decoder);
}
