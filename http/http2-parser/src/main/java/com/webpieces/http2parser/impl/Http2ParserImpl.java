package com.webpieces.http2parser.impl;

import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.CONTINUATION;
import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.HEADERS;
import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.PING;
import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.PRIORITY;
import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.PUSH_PROMISE;
import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.RST_STREAM;
import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.SETTINGS;
import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.WINDOW_UPDATE;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.webpieces.http2parser.api.FrameMarshaller;
import com.webpieces.http2parser.api.Http2Parser;
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
import com.webpieces.http2parser.api.dto.UnknownFrame;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;

public class Http2ParserImpl implements Http2Parser {
    private final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    private final BufferPool bufferPool;
    private final Map<Class<? extends Http2Frame>, FrameMarshaller> dtoToMarshaller = new HashMap<>();

	private SettingsMarshaller settingsMarshaller;

    public Http2ParserImpl(BufferPool bufferPool) {
        this.bufferPool = bufferPool;

        settingsMarshaller = new SettingsMarshaller(bufferPool, dataGen);
        dtoToMarshaller.put(DataFrame.class, new DataMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(HeadersFrame.class, new HeadersMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(PriorityFrame.class, new PriorityMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(RstStreamFrame.class, new RstStreamMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(SettingsFrame.class, settingsMarshaller);
        dtoToMarshaller.put(PushPromiseFrame.class, new PushPromiseMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(PingFrame.class, new PingMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(GoAwayFrame.class, new GoAwayMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(WindowUpdateFrame.class, new WindowUpdateMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(ContinuationFrame.class, new ContinuationMarshaller(bufferPool, dataGen));
    }

    @Override
    public ParserResult prepareToParse() {
    	return new ParserResultImpl();
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

    private byte getFrameTypeByte(Http2Frame frame) {
        return frame.getFrameType().getId();
    }

    @Override
    public int getFrameLength(Http2Frame frame) {
        FrameMarshaller marshaller = dtoToMarshaller.get(frame.getClass());
        DataWrapper payload = marshaller.marshalPayload(frame);
        return payload.getReadableSize();
    }

    @Override
    public DataWrapper marshal(Http2Frame frame) {
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
    public ParserResult parse(ParserResult memento, DataWrapper newData, long maxFrameSize) {
    	ParserResultImpl state = (ParserResultImpl) memento;
    	state.getParsedFrames().clear(); //clear any previous parsed frames
    	
        List<Http2Frame> frames = new LinkedList<>();

        DataWrapper wrapperToParse = dataGen.chainDataWrappers(state.getMoreData(), newData);

        DataWrapper wrapperToReturn = wrapperToParse; // we might return moredata if there are header framesn

        // Loop until a return (ack)
        while (true) {
            int lengthOfData = wrapperToParse.getReadableSize();
            if (lengthOfData < 9) {
                // Not even a frame header
            	state.setParsedFrames(frames);
            	state.setLeftOverData(wrapperToReturn);
            	return state;
            } else {
                // peek for length, add 9 bytes for the header
                int payloadLength =  getLength(wrapperToParse);
                int streamId = getStreamId(wrapperToParse);
                Optional<Http2FrameType> maybeFrameType = Http2FrameType.fromId(getFrameTypeId(wrapperToParse));
                maybeFrameType.ifPresent(frameType -> {
                    Integer fixedLengthForType = fixedFrameLengthByType.get(frameType);

                    long maxFrame = maxFrameSize;
                    //fetchMaxFrameSize(settings);
                    
                    if(payloadLength > maxFrame ||
                            (fixedLengthForType != null && payloadLength != fixedLengthForType) ||
                            (frameType == SETTINGS && payloadLength % 6 != 0)) {
                        boolean isConnectionLevel = connectionLevelFrames.contains(frameType) || streamId == 0x0;

                        throw new ParseException(Http2ErrorCode.FRAME_SIZE_ERROR, streamId, isConnectionLevel);
                    }
                });

                int totalLength = payloadLength + 9;
                if (lengthOfData < totalLength) {
                    // not a whole frame
                	state.setParsedFrames(frames);
                	state.setLeftOverData(wrapperToReturn);
                	return state;
                } else {
                    // parse a single frame, look for more
                    List<? extends DataWrapper> split = dataGen.split(wrapperToParse, totalLength);
                    if(maybeFrameType.isPresent()) {
                        AbstractHttp2Frame frame = unmarshal(split.get(0));

                        frames.add(frame);
                        wrapperToParse = split.get(1);
                        wrapperToReturn = wrapperToParse;
                    }
                    else {
                        // ignore this frame
                    	frames.add(new UnknownFrame((byte)0, (byte)99, 0, dataGen.emptyWrapper()));
                        wrapperToParse = split.get(1);
                        wrapperToReturn = wrapperToParse; // we set wrapperToReturn because we aren't in the middle of a headerblock
                    }
                }
            }
        }
    }

	@Override
	public SettingsFrame unmarshalSettingsPayload(ByteBuffer settingsPayload) {
		return settingsMarshaller.unmarshalPayload(settingsPayload);
	}

}
