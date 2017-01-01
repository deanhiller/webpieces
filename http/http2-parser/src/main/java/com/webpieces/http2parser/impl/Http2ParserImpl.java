package com.webpieces.http2parser.impl;

import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.CONTINUATION;
import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.HEADERS;
import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.PING;
import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.PRIORITY;
import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.PUSH_PROMISE;
import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.RST_STREAM;
import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.SETTINGS;
import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.WINDOW_UPDATE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.webpieces.http2parser.api.FrameMarshaller;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2SettingsMap;
import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.ContinuationFrame;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.HeadersFrame;
import com.webpieces.http2parser.api.dto.PingFrame;
import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.PushPromiseFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.lib.HasHeaderList;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;

public class Http2ParserImpl implements Http2Parser {
    private final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    private final BufferPool bufferPool;
    private final Map<Class<? extends AbstractHttp2Frame>, FrameMarshaller> dtoToMarshaller = new HashMap<>();

    public Http2ParserImpl(BufferPool bufferPool) {
        this.bufferPool = bufferPool;

        dtoToMarshaller.put(DataFrame.class, new DataMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(HeadersFrame.class, new HeadersMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(PriorityFrame.class, new PriorityMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(RstStreamFrame.class, new RstStreamMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(SettingsFrame.class, new SettingsMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(PushPromiseFrame.class, new PushPromiseMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(PingFrame.class, new PingMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(GoAwayFrame.class, new GoAwayMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(WindowUpdateFrame.class, new WindowUpdateMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(ContinuationFrame.class, new ContinuationMarshaller(bufferPool, dataGen));
    }

    @Override
    public FrameMarshaller getMarshaller(Class<? extends AbstractHttp2Frame> frameClass) {
        return dtoToMarshaller.get(frameClass);
    }

    @Override
    public DataWrapper prepareToParse() {
        return dataGen.emptyWrapper();
    }

    private Class<? extends AbstractHttp2Frame> getFrameClassForType(Http2FrameType type) {
        switch (type) {
            case DATA:
                return DataFrame.class;
            case HEADERS:
                return HeadersFrame.class;
            case PRIORITY:
                return PriorityFrame.class;
            case RST_STREAM:
                return RstStreamFrame.class;
            case SETTINGS:
                return SettingsFrame.class;
            case PUSH_PROMISE:
                return PushPromiseFrame.class;
            case PING:
                return PingFrame.class;
            case GOAWAY:
                return GoAwayFrame.class;
            case WINDOW_UPDATE:
                return WindowUpdateFrame.class;
            case CONTINUATION:
                return ContinuationFrame.class;
            default:
                return DataFrame.class; // TODO: change to Optional/None
        }
    }

    private int getLength(DataWrapper data) {
        ByteBuffer headerByteBuffer = bufferPool.nextBuffer(9);
        headerByteBuffer.put(data.readBytesAt(0, 9));
        headerByteBuffer.flip();

        // Get 4 bytes and just drop the rightmost one.
        return headerByteBuffer.getInt() >>> 8;
    }

    private byte getFrameTypeId(DataWrapper data) {
        return data.readByteAt(3);
    }

    private byte getFlagsByte(DataWrapper data) {
        return data.readByteAt(4);
    }

    private int getStreamId(DataWrapper data) {
        ByteBuffer streamIdBuffer = bufferPool.nextBuffer(4);
        streamIdBuffer.put(data.readBytesAt(5, 4));
        streamIdBuffer.flip();

        // Ignore the reserved bit
        return streamIdBuffer.getInt() & 0x7FFFFFFF;
    }

    // ignores what's left over at the end of the datawrapper
    @Override
    public AbstractHttp2Frame unmarshal(DataWrapper data) {
        int length = getLength(data);
        byte frameTypeId = getFrameTypeId(data);
        byte flagsByte = getFlagsByte(data);
        int streamId = getStreamId(data);
        // We require a frame type that we understand here. Invalid frame types
        // are ignored in Http2Parser.parse
        // TODO: make this unmarshal return an Optional<> so that
        // invalid frame types return Optional.empty()
        Http2FrameType frameType = Http2FrameType.fromId(frameTypeId).get();

        Class<? extends AbstractHttp2Frame> frameClass = getFrameClassForType(frameType);
        try {
            AbstractHttp2Frame frame = frameClass.newInstance();
            FrameMarshaller marshaller = dtoToMarshaller.get(frameClass);

            frame.setStreamId(streamId);
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

    private byte getFrameTypeByte(AbstractHttp2Frame frame) {
        return frame.getFrameType().getId();
    }

    @Override
    public int getFrameLength(AbstractHttp2Frame frame) {
        FrameMarshaller marshaller = dtoToMarshaller.get(frame.getClass());
        DataWrapper payload = marshaller.marshalPayload(frame);
        return payload.getReadableSize();
    }

    @Override
    public DataWrapper marshal(AbstractHttp2Frame frame) {
        FrameMarshaller marshaller = dtoToMarshaller.get(frame.getClass());

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

    @Override
    public DataWrapper marshal(List<AbstractHttp2Frame> frames) {
        DataWrapper data = dataGen.emptyWrapper();
        for(AbstractHttp2Frame frame: frames) {
            data = dataGen.chainDataWrappers(data, marshal(frame));
        }
        return data;
    }

    private static Map<Http2FrameType, Integer> fixedFrameLengthByType = new HashMap<>();
    private static List<Http2FrameType> connectionLevelFrames = new ArrayList<>();

    static {
        fixedFrameLengthByType.put(PRIORITY, 5);
        fixedFrameLengthByType.put(RST_STREAM, 4);
        fixedFrameLengthByType.put(PING, 8);
        fixedFrameLengthByType.put(WINDOW_UPDATE, 4);

        connectionLevelFrames.add(SETTINGS);
        connectionLevelFrames.add(CONTINUATION);
        connectionLevelFrames.add(HEADERS);
        connectionLevelFrames.add(PUSH_PROMISE);
        connectionLevelFrames.add(RST_STREAM);
        connectionLevelFrames.add(WINDOW_UPDATE);
    }


    @Override
    public ParserResult parse(DataWrapper oldData, DataWrapper newData, Decoder decoder, List<Http2Setting> settings) {
        DataWrapper wrapperToParse;
        List<AbstractHttp2Frame> frames = new LinkedList<>();
        List<AbstractHttp2Frame> hasHeaderFragmentList = new LinkedList<>();

        if (oldData.getReadableSize() > 0) {
            wrapperToParse = dataGen.chainDataWrappers(oldData, newData);
        }
        else {
            wrapperToParse = newData;
        }

        DataWrapper wrapperToReturn = wrapperToParse; // we might return moredata if there are header framesn

        // Loop until a return (ack)
        while (true) {
            int lengthOfData = wrapperToParse.getReadableSize();
            if (lengthOfData < 9) {
                // Not even a frame header
                return new ParserResultImpl(frames, wrapperToReturn);
            } else {
                // peek for length, add 9 bytes for the header
                int payloadLength =  getLength(wrapperToParse);
                int streamId = getStreamId(wrapperToParse);
                Optional<Http2FrameType> maybeFrameType = Http2FrameType.fromId(getFrameTypeId(wrapperToParse));
                maybeFrameType.ifPresent(frameType -> {
                    Integer fixedLengthForType = fixedFrameLengthByType.get(frameType);

                    long maxFrame = fetchMaxFrameSize(settings);
                    
                    if(payloadLength > maxFrame ||
                            (fixedLengthForType != null && payloadLength != fixedLengthForType) ||
                            (frameType == SETTINGS && payloadLength % 6 != 0)) {
                        boolean isConnectionLevel = connectionLevelFrames.contains(frameType) || streamId == 0x0;

                        throw new ParseException(Http2ErrorCode.FRAME_SIZE_ERROR, streamId, isConnectionLevel);
                    }
                });
                // If we're in the middle of a header block and we don't have a frame we recognize, throw
                if(!maybeFrameType.isPresent() && !hasHeaderFragmentList.isEmpty())
                    throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR);

                int totalLength = payloadLength + 9;
                if (lengthOfData < totalLength) {
                    // not a whole frame
                    return new ParserResultImpl(frames, wrapperToReturn);
                } else {
                    // parse a single frame, look for more
                    List<? extends DataWrapper> split = dataGen.split(wrapperToParse, totalLength);
                    if(maybeFrameType.isPresent()) {
                        Http2FrameType frameType = maybeFrameType.get();
                        AbstractHttp2Frame frame = unmarshal(split.get(0));

                        // If our headerFragmenList is non-empty, we must get a continuation frame that has the same streamid
                        // as the initial frame. This if block is just checking for error conditions. Actual
                        // processing of the header frames is in the if block that follows.
                        if (!hasHeaderFragmentList.isEmpty()) {
                            checkForBadFrames(hasHeaderFragmentList, frameType, frame);
                        }

                        // If this is a header frame, we have to make sure we get all the header
                        // frames before adding them to our framelist
                        if (Arrays.asList(HEADERS, PUSH_PROMISE, CONTINUATION).contains(frameType)) {
                            if (frameType == CONTINUATION && hasHeaderFragmentList.isEmpty()) {
                                // can't get a continuation frame if we aren't in the middle of frame processing
                                throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, frame.getStreamId(), true);
                            }
                            hasHeaderFragmentList.add(frame);
                            if (((HasHeaderFragment) frame).isEndHeaders()) {
                                doSomething(decoder, frames, hasHeaderFragmentList);
                                
                                
                                wrapperToParse = split.get(1);
                                wrapperToReturn = wrapperToParse;
                            } else {
                                wrapperToParse = split.get(1);
                                // wrapperToReturn stays unchanged because we haven't reached the end of the headers
                            }
                        } else {
                            frames.add(frame);
                            wrapperToParse = split.get(1);
                            wrapperToReturn = wrapperToParse;
                        }
                    }
                    else {
                        // ignore this frame
                        wrapperToParse = split.get(1);
                        wrapperToReturn = wrapperToParse; // we set wrapperToReturn because we aren't in the middle of a headerblock
                    }
                }
            }
        }
    }

	private long fetchMaxFrameSize(List<Http2Setting> settings) {
		for(Http2Setting s : settings) {
			if(s.getKnownName() == SettingsParameter.SETTINGS_MAX_FRAME_SIZE)
				return s.getValue();
		}
		
		int defaultMaxFrameSize = 16_384; 

		//otherwise return default
		return defaultMaxFrameSize;
	}

	private void doSomething(Decoder decoder, List<AbstractHttp2Frame> frames, List<AbstractHttp2Frame> hasHeaderFragmentList) {
		// Now we set the full header list on the first frame and just return that
		AbstractHttp2Frame firstFrame = hasHeaderFragmentList.get(0);
		DataWrapper allSerializedHeaders = dataGen.emptyWrapper();
		for (AbstractHttp2Frame iterFrame : hasHeaderFragmentList) {
		    allSerializedHeaders = dataGen.chainDataWrappers(allSerializedHeaders, ((HasHeaderFragment) iterFrame).getHeaderFragment());
		}
		((HasHeaderList) firstFrame).setHeaderList(deserializeHeaders(allSerializedHeaders, decoder));
		((HasHeaderFragment) firstFrame).setEndHeaders(true); // fake setting end headers
		frames.add(firstFrame);

		hasHeaderFragmentList.clear();
	}

	private void checkForBadFrames(List<AbstractHttp2Frame> hasHeaderFragmentList, Http2FrameType frameType, AbstractHttp2Frame frame) {
		if (frameType != CONTINUATION) {
		    throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR);
		}
		switch (hasHeaderFragmentList.get(0).getFrameType()) {
		    case PUSH_PROMISE:
		        if (frame.getStreamId() != ((PushPromiseFrame) hasHeaderFragmentList.get(0)).getPromisedStreamId()) {
		            throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR);
		        }
		        break;
		    case HEADERS:
		        if (frame.getStreamId() != hasHeaderFragmentList.get(0).getStreamId()) {
		            throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR);
		        }
		        break;
		    default:
		        throw new ParseException(Http2ErrorCode.INTERNAL_ERROR); // This should not happen
		}
	}

    @Override
    public DataWrapper serializeHeaders(LinkedList<Http2Header> headers, Encoder encoder, ByteArrayOutputStream out) {
        for (Http2Header header : headers) {
            try {
                encoder.encodeHeader(
                        out,
                        header.getName().toLowerCase().getBytes(),
                        header.getValue().getBytes(),
                        false);
            } catch (IOException e) {
                // TODO: reraise appropriately
            }
        }
        return dataGen.wrapByteArray(out.toByteArray());
    }

    @Override
    public List<AbstractHttp2Frame> createHeaderFrames(
            LinkedList<Http2Header> headers,
            Http2FrameType startingFrameType,
            int streamId,
            Http2SettingsMap remoteSettings,
            Encoder encoder,
            ByteArrayOutputStream out) {
        List<AbstractHttp2Frame> headerFrames = new LinkedList<>();

        DataWrapper serializedHeaders = serializeHeaders(headers, encoder, out);
        long maxFrameSize = remoteSettings.get(SettingsParameter.SETTINGS_MAX_FRAME_SIZE) - 16; // subtract a little to deal with the extra bits on some of the header frame types)
        boolean firstFrame = true;
        boolean lastFrame = false;
        DataWrapper fragment;
        try {
            while (true) {
                if (serializedHeaders.getReadableSize() <= maxFrameSize) {
                    lastFrame = true;
                    fragment = serializedHeaders;
                } else {
                    List<? extends DataWrapper> split = dataGen.split(serializedHeaders, (int) maxFrameSize);
                    fragment = split.get(0);
                    serializedHeaders = split.get(1);
                }

                AbstractHttp2Frame frame;
                if (firstFrame) {
                    frame = getFrameClassForType(startingFrameType).newInstance();
                    if (frame.getFrameType() == PUSH_PROMISE) {
                        // If push promise the caller will have to set the streamid of the first frame
                        ((PushPromiseFrame) frame).setPromisedStreamId(streamId);
                    } else {
                        frame.setStreamId(streamId); // for push promise the first
                    }
                } else {
                    frame = new ContinuationFrame();
                    frame.setStreamId(streamId);
                }
                ((HasHeaderFragment) frame).setHeaderFragment(fragment);
                headerFrames.add(frame);

                if (lastFrame) {
                    ((HasHeaderFragment) frame).setEndHeaders(true);
                    break;
                }
                firstFrame = false;
            }
            return headerFrames;
        }
        catch (InstantiationException | IllegalAccessException e) {
            // TODO: deal with exception here
            return null;
        }
    }

    @Override
    public LinkedList<Http2Header> deserializeHeaders(DataWrapper data, Decoder decoder) {
        LinkedList<Http2Header> headers = new LinkedList<>();

        byte[] bytes = data.createByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            decoder.decode(in, (name, value, sensitive) -> {
                        String h = new String(name);
                        String v = new String(value);
                        if(!h.equals(h.toLowerCase())) {
                            throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR);
                        }
                        headers.add(new Http2Header(h, v));
                    }
            );
        } catch (IOException e) {
            // TODO: this doesn't catch the h2spec -s 4.3 invalid header block fragment
            throw new ParseException(Http2ErrorCode.COMPRESSION_ERROR);
        }
        decoder.endHeaderBlock();
        return headers;
    }
}
