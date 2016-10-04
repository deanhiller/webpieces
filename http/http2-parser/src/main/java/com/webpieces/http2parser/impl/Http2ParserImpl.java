package com.webpieces.http2parser.impl;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.twitter.hpack.HeaderListener;
import com.webpieces.http2parser.api.*;
import com.webpieces.http2parser.api.dto.*;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class Http2ParserImpl implements Http2Parser {
    private final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    private final BufferPool bufferPool;
    private final Map<Class<? extends Http2Frame>, FrameMarshaller> dtoToMarshaller = new HashMap<>();

    public Http2ParserImpl(BufferPool bufferPool) {
        this.bufferPool = bufferPool;

        dtoToMarshaller.put(Http2Data.class, new DataMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(Http2Headers.class, new HeadersMarshaller(bufferPool, dataGen, this));
        dtoToMarshaller.put(Http2Priority.class, new PriorityMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(Http2RstStream.class, new RstStreamMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(Http2Settings.class, new SettingsMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(Http2PushPromise.class, new PushPromiseMarshaller(bufferPool, dataGen, this));
        dtoToMarshaller.put(Http2Ping.class, new PingMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(Http2GoAway.class, new GoAwayMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(Http2WindowUpdate.class, new WindowUpdateMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(Http2Continuation.class, new ContinuationMarshaller(bufferPool, dataGen, this));
    }

    public DataWrapper prepareToParse() {
        return dataGen.emptyWrapper();
    }

    // includes header length
    private int peekLengthOfFrame(DataWrapper data) {
        ByteBuffer lengthBytes = ByteBuffer.wrap(data.readBytesAt(0, 3));
        int length = lengthBytes.getShort() << 8;
        length |= lengthBytes.get();
        return length + 9; // add 9 bytes for the header
    }

    private Class<? extends Http2Frame> getFrameClassForType(Http2FrameType type) {
        switch (type) {
            case DATA:
                return Http2Data.class;
            case HEADERS:
                return Http2Headers.class;
            case PRIORITY:
                return Http2Priority.class;
            case RST_STREAM:
                return Http2RstStream.class;
            case SETTINGS:
                return Http2Settings.class;
            case PUSH_PROMISE:
                return Http2PushPromise.class;
            case PING:
                return Http2Ping.class;
            case GOAWAY:
                return Http2GoAway.class;
            case WINDOW_UPDATE:
                return Http2WindowUpdate.class;
            case CONTINUATION:
                return Http2Continuation.class;
            default:
                return Http2Data.class; // TODO: change to Optional/None
        }
    }

    // ignores what's left over at the end of the datawrapper
    public Http2Frame unmarshal(DataWrapper data) {
        ByteBuffer headerByteBuffer = bufferPool.nextBuffer(9);
        headerByteBuffer.put(data.readBytesAt(0, 9));
        headerByteBuffer.flip();

        int length = headerByteBuffer.getShort() << 8;
        length |= headerByteBuffer.get();

        byte frameTypeId = headerByteBuffer.get();

        Class<? extends Http2Frame> frameClass = getFrameClassForType(Http2FrameType.fromId(frameTypeId));
        try {
            Http2Frame frame = frameClass.newInstance();
            FrameMarshaller marshaller = dtoToMarshaller.get(frameClass);

            byte flagsByte = headerByteBuffer.get();

            // Ignore the reserved bit
            int streamId = headerByteBuffer.getInt();
            frame.setStreamId(streamId);
            bufferPool.releaseBuffer(headerByteBuffer);

            Optional<DataWrapper> maybePayload;

            if (length > 0) {
                List<? extends DataWrapper> splitWrappers = dataGen.split(data, 9);
                DataWrapper payloadPlusMore = splitWrappers.get(1);
                List<? extends DataWrapper> split = dataGen.split(payloadPlusMore, length);
                maybePayload = Optional.of(split.get(0));
            } else {
                maybePayload = Optional.empty();
            }

            marshaller.unmarshalFlagsAndPayload(frame, flagsByte, maybePayload);

            return frame;

        } catch (InstantiationException | IllegalAccessException e) {
            // TODO: deal with exception
            return null; // should reraise in some fashion
        }

    }

    private byte getFrameTypeByte(Http2Frame frame) {
        return frame.getFrameType().getId();
    }

    public DataWrapper marshal(Http2Frame frame) {
        FrameMarshaller marshaller = dtoToMarshaller.get(frame.getClass());
        // Look in ObjectTranslator for classtoMarshaller examples, use bufferPools

        if(marshaller == null)
            return null; //throw here

        ByteBuffer header = ByteBuffer.allocate(9);
        DataWrapper payload = marshaller.marshalPayload(frame);

        int length = payload.getReadableSize();
        header.put((byte) (length >>> 16));
        header.putShort((short) length);

        header.put(getFrameTypeByte(frame));
        header.put(marshaller.marshalFlags(frame));

        // 1 bit reserved, streamId MSB is always 0, see setStreamId()
        header.putInt(frame.getStreamId());
        header.flip();

        return dataGen.chainDataWrappers(dataGen.wrapByteBuffer(header), payload);
    }

    public ParserResult parse(DataWrapper oldData, DataWrapper newData) {
        DataWrapper wrapperToParse;
        List<Http2Frame> frames = new ArrayList<>();

        if (oldData.getReadableSize() > 0)
            wrapperToParse = dataGen.chainDataWrappers(oldData, newData);
        else
            wrapperToParse = newData;

        // Loop until a return (ack)
        while (true) {
            int lengthOfData = wrapperToParse.getReadableSize();
            if (lengthOfData <= 3) {
                // Not even a length
                return new ParserResultImpl(frames, wrapperToParse);
            } else {
                // peek for length
                int length = peekLengthOfFrame(wrapperToParse);
                if (lengthOfData < length) {
                    // not a whole frame
                    return new ParserResultImpl(frames, wrapperToParse);
                } else {
                    // parse a single frame, look for more
                    List<? extends DataWrapper> split = dataGen.split(wrapperToParse, length);
                    Http2Frame frame = unmarshal(split.get(0));
                    frames.add(frame);
                    wrapperToParse = split.get(1);
                }
            }
        }
    }

    public DataWrapper createSerializedHeaders(LinkedList<HasHeaders.Header> headers) {
        // TODO: get from settings
        Encoder encoder = new Encoder(4096);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (HasHeaders.Header header : headers) {
            try {
                encoder.encodeHeader(
                        out,
                        header.header.getBytes(),
                        header.value.getBytes(),
                        false);
            } catch (IOException e) {
                // TODO: reraise appropriately
            }
        }
        return dataGen.wrapByteArray(out.toByteArray());
    }

    public List<Http2Frame> createHeaderFrames(LinkedList<HasHeaders.Header> headers, Class<? extends HasHeaders> startingFrameType, int streamId) {
        List<Http2Frame> headerFrames = new ArrayList<>();

        // Only create one for now
        try {
            HasHeaders frame = startingFrameType.newInstance();
            ((Http2Frame) frame).setStreamId(streamId);

            frame.setHeaders(headers);
            frame.setEndHeaders(true);
            frame.setSerializedHeaders(createSerializedHeaders(headers));
            headerFrames.add((Http2Frame) frame);
            return headerFrames;
        } catch (IllegalAccessException | InstantiationException e) {
            // TODO: deal with exception
            return null;
        }
    }

    public LinkedList<HasHeaders.Header> deserializeHeaders(DataWrapper data) {
        LinkedList<HasHeaders.Header> headers = new LinkedList<>();

        byte[] bytes = data.createByteArray();
        // TODO: get maxs from settings
        Decoder decoder = new Decoder(4096, 4096);
        HeaderListener listener = new HeaderListener() {
            @Override
            public void addHeader(byte[] name, byte[] value, boolean sensitive) {
                headers.add(new HasHeaders.Header(new String(name).toLowerCase(), new String(value)));
            }
        };
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            decoder.decode(in, listener);
        } catch (IOException e) {
            // TODO: reraise appropriately here
        }
        decoder.endHeaderBlock();
        return headers;
    }
}
