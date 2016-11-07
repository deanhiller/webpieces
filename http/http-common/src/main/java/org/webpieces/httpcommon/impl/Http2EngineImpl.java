package org.webpieces.httpcommon.impl;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2SettingsMap;
import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.*;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpcommon.api.*;
import org.webpieces.httpcommon.api.exceptions.*;
import org.webpieces.httpcommon.api.exceptions.InternalError;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.*;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntUnaryOperator;

import static com.webpieces.http2parser.api.dto.Http2FrameType.HEADERS;
import static com.webpieces.http2parser.api.dto.Http2FrameType.PUSH_PROMISE;
import static com.webpieces.http2parser.api.dto.Http2FrameType.SETTINGS;
import static com.webpieces.http2parser.api.dto.Http2Settings.Parameter.*;
import static com.webpieces.http2parser.api.dto.Http2Settings.Parameter.SETTINGS_HEADER_TABLE_SIZE;
import static com.webpieces.http2parser.api.dto.Http2Settings.Parameter.SETTINGS_MAX_FRAME_SIZE;
import static java.lang.Math.min;
import static org.webpieces.httpcommon.api.Http2Engine.HttpSide.CLIENT;
import static org.webpieces.httpcommon.api.Http2Engine.HttpSide.SERVER;
import static org.webpieces.httpcommon.impl.Stream.StreamStatus.CLOSED;
import static org.webpieces.httpcommon.impl.Stream.StreamStatus.HALF_CLOSED_REMOTE;
import static org.webpieces.httpcommon.impl.Stream.StreamStatus.IDLE;
import static org.webpieces.httpparser.api.common.KnownHeaderName.TRAILER;
import static org.webpieces.httpparser.api.dto.HttpRequest.HttpScheme.HTTPS;

public abstract class Http2EngineImpl implements Http2Engine {
    private static final Logger log = LoggerFactory.getLogger(Http2EngineImpl.class);
    private static DataWrapperGenerator wrapperGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    static String prefaceHexString = "505249202a20485454502f322e300d0a0d0a534d0d0a0d0a";

    private Channel channel;
    DataListener dataListener;
    private Http2Parser http2Parser;
    private InetSocketAddress remoteAddress;

    private HttpSide side;

    private Http2SettingsMap localRequestedSettings = new Http2SettingsMap();

    // remotesettings doesn't need concurrent bc listener is vts
    Http2SettingsMap remoteSettings = new Http2SettingsMap();
    private AtomicBoolean gotSettings = new AtomicBoolean(false);

    // localsettings also doesn't need concurrent bc local settings is only set when
    // it gets the ack from the settings that gets sent.
    private Http2SettingsMap localSettings = new Http2SettingsMap();

    ConcurrentHashMap<Integer, Stream> activeStreams = new ConcurrentHashMap<>();
    private AtomicInteger nextOutgoingStreamId;
    private AtomicInteger lastIncomingStreamId = new AtomicInteger(0);
    private ConcurrentHashMap<Integer, AtomicLong> outgoingFlowControl = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, AtomicLong> incomingFlowControl = new ConcurrentHashMap<>();
    private class PendingData {
        CompletableFuture<Void> future;
        DataWrapper data;
        boolean isComplete;
        Stream stream;

        PendingData(CompletableFuture<Void> future, DataWrapper data, boolean isComplete, Stream stream) {
            this.future = future;
            this.data = data;
            this.isComplete = isComplete;
            this.stream = stream;
        }
    }

    private ConcurrentHashMap<Integer, ConcurrentLinkedDeque<PendingData>> outgoingDataQueue = new ConcurrentHashMap<>();

    private Encoder encoder;
    private Decoder decoder;
    private AtomicBoolean maxHeaderTableSizeNeedsUpdate = new AtomicBoolean(false);
    private AtomicInteger minimumMaxHeaderTableSizeUpdate = new AtomicInteger(Integer.MAX_VALUE);

    // TODO: figure out how to deal with the goaway. For now we're just
    // going to record what they told us.
    // these don't have to be concurrent-safe because the datalistener is virtually single threaded.
    private boolean remoteGoneAway = false;
    private int goneAwayLastStreamId;
    private Http2ErrorCode goneAwayErrorCode;
    private DataWrapper additionalDebugData;


    public Http2EngineImpl(
        Http2Parser http2Parser,
        Channel channel,
        InetSocketAddress remoteAddress,
        Http2SettingsMap http2SettingsMap,
        HttpSide side)
    {
        this.http2Parser = http2Parser;
        this.channel = channel;
        this.remoteAddress = remoteAddress;
        this.localRequestedSettings = http2SettingsMap;
        this.side = side;
        if(side == CLIENT) {
            this.nextOutgoingStreamId = new AtomicInteger(0x1);
        }
        else {
            this.nextOutgoingStreamId = new AtomicInteger(0x2);
        }

        // Initialize to defaults
        remoteSettings.put(SETTINGS_HEADER_TABLE_SIZE, 4096L);
        localSettings.put(SETTINGS_HEADER_TABLE_SIZE, 4096L);

        remoteSettings.put(SETTINGS_ENABLE_PUSH, 1L);
        localSettings.put(SETTINGS_ENABLE_PUSH, 1L);

        // No limit for MAX_CONCURRENT_STREAMS by default so it isn't in the map

        remoteSettings.put(SETTINGS_INITIAL_WINDOW_SIZE, 65535L);
        localSettings.put(SETTINGS_INITIAL_WINDOW_SIZE, 65535L);

        remoteSettings.put(SETTINGS_MAX_FRAME_SIZE, 16384L);
        localSettings.put(SETTINGS_MAX_FRAME_SIZE, 16384L);

        // No limit for MAX_HEADER_LIST_SIZE by default, so not in the map

        this.decoder = new Decoder(4096, localSettings.get(SETTINGS_HEADER_TABLE_SIZE).intValue());
        this.encoder = new Encoder(remoteSettings.get(SETTINGS_HEADER_TABLE_SIZE).intValue());

        this.dataListener = new Http2DataListener();

        initializeFlowControl(0x0);

        // set some default preferred settings locally
        // TODO: make this configurable by the customer
        localRequestedSettings.put(SETTINGS_MAX_CONCURRENT_STREAMS, 100L);
        //localRequestedSettings.put(SETTINGS_MAX_HEADER_LIST_SIZE, 100)
    }

    @Override
    public Channel getUnderlyingChannel() {
        return channel;
    }


    @Override
    public void sendLocalRequestedSettings() {
        Http2Settings settingsFrame = new Http2Settings();

        settingsFrame.setSettings(localRequestedSettings);
        log.info("sending settings: " + settingsFrame);
        channel.write(ByteBuffer.wrap(http2Parser.marshal(settingsFrame).createByteArray()));
    }

    void setRemoteSettings(Http2Settings frame, boolean sendAck) {
        // We've received a settings. Check for legit-ness.
        if(frame.getSettings().get(SETTINGS_ENABLE_PUSH) != null && (
                frame.getSettings().get(SETTINGS_ENABLE_PUSH) != 0 || frame.getSettings().get(SETTINGS_ENABLE_PUSH) != 1))
            throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());

        // 2^31 - 1 - max flow control window
        if(frame.getSettings().get(SETTINGS_INITIAL_WINDOW_SIZE) != null &&
                frame.getSettings().get(SETTINGS_INITIAL_WINDOW_SIZE) > 2147483647)
            throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.FLOW_CONTROL_ERROR, wrapperGen.emptyWrapper());

        // frame size must be between 16384 and 2^24 - 1
        if(frame.getSettings().get(SETTINGS_MAX_FRAME_SIZE) != null && (
                frame.getSettings().get(SETTINGS_MAX_FRAME_SIZE) < 16384 || frame.getSettings().get(SETTINGS_MAX_FRAME_SIZE) > 1677215))
            throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());

        // Update remoteSettings
        log.info("Setting remote settings to: " + frame.getSettings());
        gotSettings.set(true);
        for(Map.Entry<Http2Settings.Parameter, Long> entry: frame.getSettings().entrySet()) {
            remoteSettings.put(entry.getKey(), entry.getValue());
        }

        // What do we do when certain settings are updated
        if(frame.getSettings().containsKey(SETTINGS_HEADER_TABLE_SIZE)) {
            maxHeaderTableSizeNeedsUpdate.set(true);
            class UpdateMinimum implements IntUnaryOperator {
                int newTableSize;

                public UpdateMinimum(int newTableSize) {
                    this.newTableSize = newTableSize;
                }

                @Override
                public int applyAsInt(int operand) {
                    return min(operand, newTableSize);
                }
            }
            minimumMaxHeaderTableSizeUpdate.updateAndGet(
                    new UpdateMinimum(frame.getSettings().get(SETTINGS_HEADER_TABLE_SIZE).intValue()));
        }
        if(sendAck) {
            Http2Settings responseFrame = new Http2Settings();
            responseFrame.setAck(true);
            log.info("sending settings ack: " + responseFrame);
            channel.write(ByteBuffer.wrap(http2Parser.marshal(responseFrame).createByteArray()));
        }
    }

    @Override
    public void startPing() {

        Timer timer = new Timer();
        // in 5 seconds send a ping every 5 seconds
        timer.schedule(new SendPing(), 5000, 5000);
    }

    @Override
    public DataListener getDataListener() {
        return dataListener;
    }

    Http2Settings getLocalRequestedSettingsFrame() {
        Http2Settings settingsFrame = new Http2Settings();
        settingsFrame.setSettings(localRequestedSettings);
        return settingsFrame;
    }

    LinkedList<HasHeaderFragment.Header> requestToHeaders(HttpRequest request) {
        HttpRequestLine requestLine = request.getRequestLine();
        List<Header> requestHeaders = request.getHeaders();

        LinkedList<HasHeaderFragment.Header> headerList = new LinkedList<>();

        // add special headers
        headerList.add(new HasHeaderFragment.Header(":method", requestLine.getMethod().getMethodAsString()));

        UrlInfo urlInfo = requestLine.getUri().getUriBreakdown();
        headerList.add(new HasHeaderFragment.Header(":path", urlInfo.getFullPath()));

        // Figure out scheme
        if(urlInfo.getPrefix() != null) {
            headerList.add(new HasHeaderFragment.Header(":scheme", urlInfo.getPrefix()));
        } else {
            if(channel.isSslChannel()) {
                headerList.add(new HasHeaderFragment.Header(":scheme", "https"));
            } else {
                headerList.add(new HasHeaderFragment.Header(":scheme", "http"));
            }
        }

        // Figure out authority
        String h = null;
        for(Header header: requestHeaders) {
            if(header.getKnownName().equals(KnownHeaderName.HOST)) {
                h = header.getValue();
                break;
            }
        }
        if(h != null) {
            headerList.add(new HasHeaderFragment.Header(":authority", h));
        } else {
            if (urlInfo.getHost() != null) {
                if (urlInfo.getPort() == null)
                    headerList.add(new HasHeaderFragment.Header(":authority", urlInfo.getHost()));
                else
                    headerList.add(new HasHeaderFragment.Header(":authority", String.format("%s:%d", urlInfo.getHost(), urlInfo.getPort())));
            } else {
                headerList.add(new HasHeaderFragment.Header(":authority", remoteAddress.getHostName() + ":" + remoteAddress.getPort()));
            }
        }

        // Add regular headers
        for(Header header: requestHeaders) {
            headerList.add(new HasHeaderFragment.Header(header.getName().toLowerCase(), header.getValue()));
        }

        return headerList;
    }


    private void clearQueue(int streamId) {
        ConcurrentLinkedDeque<PendingData> queue = outgoingDataQueue.get(streamId);
        while (queue != null && !queue.isEmpty() && outgoingFlowControl.get(streamId).get() > 0) {
            PendingData pendingData = queue.poll();
            log.info("sending data from the queue: " + pendingData.data);
            sendDataFrames(pendingData.data, pendingData.isComplete, pendingData.stream, true)
                    .thenAccept(v -> pendingData.future.complete(null));
        }
    }

    private CompletableFuture<Void> writeDataFrame(Http2Data frame) {
        int streamId = frame.getStreamId();
        log.info("actually writing data frame: " + frame);
        decrementOutgoingWindow(streamId, http2Parser.getFrameLength(frame));
        return channel.write(ByteBuffer.wrap(http2Parser.marshal(frame).createByteArray())).thenAccept(c -> {});
    }

    private boolean isQueueEmpty(Stream stream) {
        ConcurrentLinkedDeque<PendingData> queue = outgoingDataQueue.get(stream.getStreamId());
        return (queue == null || queue.isEmpty());
    }

    private CompletableFuture<Void> queueData(DataWrapper body, boolean isComplete, Stream stream, boolean putAtFrontOfQueue) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        PendingData pendingData = new PendingData(future, body, isComplete, stream);
        ConcurrentLinkedDeque<PendingData> queue = outgoingDataQueue.get(stream.getStreamId());
        if (queue == null) {
            queue = new ConcurrentLinkedDeque<>();
            outgoingDataQueue.put(stream.getStreamId(), queue);
        }

        if(putAtFrontOfQueue) {
            log.info("placing data at the front of the queue: " + body);
            queue.addFirst(pendingData);
        } else {
            log.info("placing data at the back of the queue: " + body);
            queue.addLast(pendingData);
        }
        return future;
    }

    CompletableFuture<Void> sendDataFrames(DataWrapper body, boolean isComplete, Stream stream, boolean wasFrontOfQueue) {
        switch(stream.getStatus()) {
            case OPEN:
            case HALF_CLOSED_REMOTE:
                // If there's things on the queue and we didn't get this from the front of the queue,
                // queue this data.
                if(!isQueueEmpty(stream) && !wasFrontOfQueue) {
                    log.info("queueing data: " + body);
                    return queueData(body, isComplete, stream, false);
                }

                long maxLength = min(remoteSettings.get(SETTINGS_MAX_FRAME_SIZE),
                        min(outgoingFlowControl.get(stream.getStreamId()).get(),
                                outgoingFlowControl.get(0x0).get()));
                int dataLength = body.getReadableSize();

                Http2Data newFrame = new Http2Data();
                newFrame.setStreamId(stream.getStreamId());

                if(dataLength <= maxLength) {
                    // writes only one frame at a time.
                    // the body fits within one frame and is within the flow control window
                    newFrame.setData(body);
                    if(isComplete)
                        newFrame.setEndStream(true);

                    log.info("writing final data frame: (but might not complete the request)" + newFrame);
                    return writeDataFrame(newFrame).thenAccept(
                            v -> {
                                if (isComplete) {
                                    switch(stream.getStatus()) {
                                        case OPEN:
                                            stream.setStatus(Stream.StreamStatus.HALF_CLOSED_LOCAL);
                                            break;
                                        case HALF_CLOSED_REMOTE:
                                            stream.setStatus(CLOSED);
                                            break;
                                        default:
                                            throw new ClientError("can't send endstream on a stream in state " + stream.getStatus());
                                    }
                                }
                            }
                    );
                } else {
                    // too big, if possible, split it to fit within framesize or window size send, and recurse.
                    if(maxLength > 0) {
                        // We have space in the window, send something.
                        List<? extends DataWrapper> split = wrapperGen.split(body, (int) maxLength);
                        newFrame.setData(split.get(0));
                        log.info("writing non-final data frame: " + newFrame);
                        return writeDataFrame(newFrame).thenCompose(
                                v -> sendDataFrames(split.get(1), isComplete, stream, wasFrontOfQueue)
                        );
                    } else {
                        // If this data was in the queue, then put it back at the front of the queue.
                        return queueData(body, isComplete, stream, wasFrontOfQueue);
                    }
                }
            default:
                throw new ClientError(
                        String.format("can't send data on a stream in state %s", stream.getStatus().toString()));
        }

    }

    private void updateMaxHeaderTableSize(ByteArrayOutputStream out) {
        // If the header table size needs update, we pre-fill the buffer with the update notification

        try {
            if (maxHeaderTableSizeNeedsUpdate.get()) {
                // If we need to update the max header table size
                int newMaxHeaderTableSize = remoteSettings.get(SETTINGS_HEADER_TABLE_SIZE).intValue();
                if (minimumMaxHeaderTableSizeUpdate.get() < newMaxHeaderTableSize) {
                    encoder.setMaxHeaderTableSize(out, minimumMaxHeaderTableSizeUpdate.get());
                }
                encoder.setMaxHeaderTableSize(out, newMaxHeaderTableSize);
                minimumMaxHeaderTableSizeUpdate.set(Integer.MAX_VALUE);
                maxHeaderTableSizeNeedsUpdate.set(false);
            }
        } catch (IOException e) {
            // TODO: Remove debugdata when not in developer mode
            throw new InternalError(lastClosedRemoteOriginatedStream().orElse(0), wrapperGen.wrapByteArray(e.toString().getBytes()));
        }
    }
    CompletableFuture<Void> sendPushPromiseFrames(LinkedList<HasHeaderFragment.Header> headerList, Stream stream, Stream newStream) {
        int streamId = stream.getStreamId();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        updateMaxHeaderTableSize(out);

        List<Http2Frame> frameList = http2Parser.createHeaderFrames(headerList, PUSH_PROMISE, newStream.getStreamId(), remoteSettings, encoder, out);
        // Set the streamid in the first frame to this streamid
        frameList.get(0).setStreamId(streamId);

        // Send all the frames at once
        log.info("sending push promise frames: " + frameList);
        return channel.write(ByteBuffer.wrap(http2Parser.marshal(frameList).createByteArray())).thenAccept(
                channel -> newStream.setStatus(Stream.StreamStatus.RESERVED_LOCAL)
        );

    }
    // we never send endstream on the header frame to make our life easier. we always just send
    // endstream on a data frame.
    CompletableFuture<Void> sendHeaderFrames(LinkedList<HasHeaderFragment.Header> headerList, Stream stream) {
        // TODO: check the status of the stream to ensure we can send HEADER frames

        int streamId = stream.getStreamId();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        updateMaxHeaderTableSize(out);

        List<Http2Frame> frameList = http2Parser.createHeaderFrames(headerList, HEADERS, streamId, remoteSettings, encoder, out);

        // Send all the frames at once
        log.info("sending header frames: " + frameList);
        return channel.write(ByteBuffer.wrap(http2Parser.marshal(frameList).createByteArray())).thenAccept(
                channel -> {
                    switch (stream.getStatus()) {
                        case IDLE:
                            stream.setStatus(Stream.StreamStatus.OPEN);
                            break;
                        case HALF_CLOSED_LOCAL:
                        case HALF_CLOSED_REMOTE:
                            // leave status the same
                            break;
                        case RESERVED_LOCAL:
                            stream.setStatus(HALF_CLOSED_REMOTE);
                            break;
                        default:
                            throw new InternalError(lastClosedRemoteOriginatedStream().orElse(0), wrapperGen.emptyWrapper()); //"should not be sending headers on a stream not open or half closed remote"
                    }
                }
        );
    }

    private long countOpenStreams(HttpSide side) {
        int mod = side == CLIENT ? 1 : 0;
        return activeStreams.entrySet().stream().filter(entry -> {
            Stream.StreamStatus status = entry.getValue().getStatus();
            boolean open = (Arrays.asList(Stream.StreamStatus.OPEN, Stream.StreamStatus.HALF_CLOSED_LOCAL, Stream.StreamStatus.HALF_CLOSED_REMOTE).contains(status));
            boolean server = entry.getValue().getStreamId() % 2 == mod;
            return open && server;
        }).count();
    }
    private long countOpenRemoteOriginatedStreams() {
        if(side == CLIENT)
            return countOpenStreams(SERVER);
        else
            return countOpenStreams(CLIENT);
    }

    long countOpenLocalOriginatedStreams() {
        return countOpenStreams(side);
    }

    private Optional<Integer> lastClosedStream(HttpSide side) {
        int mod = side == CLIENT ? 1 : 0;
        return activeStreams.entrySet()
                .stream()
                .filter(entry -> (entry.getValue().getStatus() == CLOSED) && (entry.getValue().getStreamId() % 2 == mod))
                .max(Comparator.comparingInt(Map.Entry::getKey)).map(entry -> entry.getKey());
    }

    private Optional<Integer> lastClosedRemoteOriginatedStream() {
        if(side == CLIENT)
            return lastClosedStream(SERVER);
        else
            return lastClosedStream(CLIENT);

    }

    private Optional<Integer> lastClosedLocalOriginatedStream() {
        return lastClosedStream(side);
    }

    int getAndIncrementStreamId() {
        return nextOutgoingStreamId.getAndAdd(2);
    }

    private class SendPing extends TimerTask {
        @Override
        public void run() {
            Http2Ping pingFrame = new Http2Ping();
            pingFrame.setOpaqueData(System.nanoTime());
            channel.write(ByteBuffer.wrap(http2Parser.marshal(pingFrame).createByteArray()));
        }
    }


    private void decrementOutgoingWindow(int streamId, int length) {
        log.info("decrementing outgoing window for {} by {}", streamId, length);
        if(outgoingFlowControl.get(0x0).addAndGet(- length) < 0) {
            throw new RuntimeException("this should not happen");
        }
        if(outgoingFlowControl.get(streamId).addAndGet(- length) < 0) {
            throw new RuntimeException("this should not happen");
        }
        log.info("stream {} outgoing window is {} and connection window is {}", streamId, outgoingFlowControl.get(streamId), outgoingFlowControl.get(0));
    }

    private void decrementIncomingWindow(int streamId, int length) {
        log.info("decrementing incoming window for {} by {}", streamId, length);
        if(incomingFlowControl.get(0x0).addAndGet(- length) < 0) {
            throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.FLOW_CONTROL_ERROR,
                    wrapperGen.emptyWrapper());
        }
        if(incomingFlowControl.get(streamId).addAndGet(- length) < 0) {
            throw new RstStreamError(Http2ErrorCode.FLOW_CONTROL_ERROR, streamId);
        }
        log.info("stream {} incoming window is {} and connection window is {}", streamId, incomingFlowControl.get(streamId), incomingFlowControl.get(0));
    }

    private void incrementOutgoingWindow(int streamId, int length) {
        log.info("incrementing outgoing window for {} by {}", streamId, length);
        if(outgoingFlowControl.get(0x0).addAndGet(length) > 2147483647L)
            throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.FLOW_CONTROL_ERROR, wrapperGen.emptyWrapper());

        if(outgoingFlowControl.get(streamId) == null)
            initializeFlowControl(streamId);

        if(outgoingFlowControl.get(streamId).addAndGet(length) > 2147483647L)
            throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), streamId, Http2ErrorCode.FLOW_CONTROL_ERROR, wrapperGen.emptyWrapper());

        log.info("stream {} outgoing window is {} and connection window is {}", streamId, outgoingFlowControl.get(streamId), outgoingFlowControl.get(0));
    }

    void incrementIncomingWindow(int streamId, int length) {
        log.info("incrementing incoming window for {} by {}", streamId, length);
        incomingFlowControl.get(0x0).addAndGet(length);
        incomingFlowControl.get(streamId).addAndGet(length);

        Http2WindowUpdate frame = new Http2WindowUpdate();
        frame.setWindowSizeIncrement(length);
        frame.setStreamId(0x0);
        channel.write(ByteBuffer.wrap(http2Parser.marshal(frame).createByteArray()));

        // reusing the frame! ack.
        frame.setStreamId(streamId);
        channel.write(ByteBuffer.wrap(http2Parser.marshal(frame).createByteArray()));
        log.info("stream {} incoming window is {} and connection window is {}", streamId, incomingFlowControl.get(streamId), incomingFlowControl.get(0));
    }

    void initializeFlowControl(int streamId) {
        // Set up flow control
        incomingFlowControl.put(streamId, new AtomicLong(localSettings.get(SETTINGS_INITIAL_WINDOW_SIZE)));
        outgoingFlowControl.put(streamId, new AtomicLong(remoteSettings.get(SETTINGS_INITIAL_WINDOW_SIZE)));
    }

    LinkedList<HasHeaderFragment.Header> responseToHeaders(HttpResponse response) {
        LinkedList<HasHeaderFragment.Header> headers = new LinkedList<>();
        headers.add(new HasHeaderFragment.Header(":status", response.getStatusLine().getStatus().getCode().toString()));
        for(Header header: response.getHeaders()) {
            headers.add(new HasHeaderFragment.Header(header.getName(), header.getValue()));
        }
        return headers;
    }

    abstract void sideSpecificHandleData(Http2Data frame, int payloadLength, Stream stream);

    abstract void sideSpecificHandleHeaders(Http2Headers frame, boolean isTrailer, Stream stream);

    abstract void sideSpecificHandleRstStream(Http2RstStream frame, Stream stream);

    private void receivedEndStream(Stream stream) {
        // Make sure status can accept ES
        switch(stream.getStatus()) {
            case OPEN:
                stream.setStatus(Stream.StreamStatus.HALF_CLOSED_REMOTE);
                break;
            case HALF_CLOSED_LOCAL:
                stream.setStatus(CLOSED);
                break;
            default:
                throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
        }
    }

    private Map<String, String> processSpecialHeaders(Queue<HasHeaderFragment.Header> headers,
                                                      List<String> specialHeaders,
                                                      HttpMessage msg,
                                                      int streamId) {
        Map<String, String> specialHeaderMap = new HashMap<>();

        boolean processingSpecialHeaders = true;
        for(HasHeaderFragment.Header header: headers) {
            if(processingSpecialHeaders) {
                if(!header.header.startsWith((":")))
                {
                    processingSpecialHeaders = false;
                } else {
                    // If we got a special header twice, or a special header we are not expecting
                    if (specialHeaderMap.get(header.header) != null || !specialHeaders.contains(header.header)) {
                        throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, streamId);
                    }
                    specialHeaderMap.put(header.header, header.value);
                }
            }
            if(!processingSpecialHeaders) {
                // if we got a special header mixed in with the regular headers
                if(header.header.startsWith(":")) {
                    throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, streamId);
                }
                msg.addHeader(new Header(header.header, header.value));
            }
        }

        // Make sure we got all the special headers
        for(String specialHeader: specialHeaders) {
            if(specialHeaderMap.get(specialHeader) == null) {
                throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, streamId);
            }
        }
        return specialHeaderMap;

    }

    HttpResponse responseFromHeaders(Queue<HasHeaderFragment.Header> headers, Stream stream) {
        HttpResponse response = new HttpResponse();

        Map<String, String> specialHeaderMap = processSpecialHeaders(
                headers, Arrays.asList(":status"), response, stream.getStreamId());

        HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
        HttpResponseStatus status = new HttpResponseStatus();
        try {
            status.setKnownStatus(KnownStatusCode.lookup(Integer.parseInt(specialHeaderMap.get(":status"))));
        } catch(NumberFormatException e) {
            throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
        }

        statusLine.setStatus(status);
        HttpVersion version = new HttpVersion();
        version.setVersion("2.0");
        statusLine.setVersion(version);

        response.setStatusLine(statusLine);
        return response;
    }

    HttpRequest requestFromHeaders(Queue<HasHeaderFragment.Header> headers, Stream stream) {
        HttpRequest request = new HttpRequest();
        Map<String, String> specialHeaderMap = processSpecialHeaders(
                headers,
                Arrays.asList(":method", ":path", ":authority", ":scheme"),
                request,
                stream.getStreamId());

        // See https://svn.tools.ietf.org/svn/wg/httpbis/specs/rfc7230.html#asterisk-form
        if(specialHeaderMap.get(":method").toLowerCase().equals("options") && specialHeaderMap.get(":path").equals("*")) {
            specialHeaderMap.put(":path", "");
        }

        HttpRequestLine requestLine = new HttpRequestLine();
        requestLine.setUri(new HttpUri(String.format(
                "%s://%s%s",
                specialHeaderMap.get(":scheme"),
                specialHeaderMap.get(":authority"),
                specialHeaderMap.get(":scheme"))));
        switch(specialHeaderMap.get(":scheme").toLowerCase()) {
            case "http":
                request.setHttpScheme(HttpRequest.HttpScheme.HTTP);
                break;
            case "https":
                request.setHttpScheme(HTTPS);
                break;
            default:
                throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
        }

        requestLine.setMethod(new HttpRequestMethod(specialHeaderMap.get(":method")));
        HttpVersion version = new HttpVersion();
        version.setVersion("2.0");
        requestLine.setVersion(version);
        request.setRequestLine(requestLine);

        return request;
    }

    void checkHeaders(Headers headerLookupStruct, Stream stream) {
        if(headerLookupStruct.getHeader(KnownHeaderName.CONTENT_LENGTH) != null) {
            stream.setContentLengthHeaderValue(Long.parseLong(headerLookupStruct.getHeader(KnownHeaderName.CONTENT_LENGTH).getValue()));
        }
        if(headerLookupStruct.getHeader(KnownHeaderName.CONNECTION) != null) {
            throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), stream.getStreamId(), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());
        }
        if(headerLookupStruct.getHeader(KnownHeaderName.TE) != null && !headerLookupStruct.getHeader(KnownHeaderName.TE).getValue().toLowerCase().equals("trailers")) {
            throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), stream.getStreamId(), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());
        }
        if(headerLookupStruct.getHeaders(TRAILER) != null) {
            for(Header header: headerLookupStruct.getHeaders(TRAILER)) {
                stream.addTrailerHeader(header.getValue().toLowerCase());
            }
        }
    }

    private class Http2DataListener implements DataListener {
        private DataWrapper oldData = http2Parser.prepareToParse();
        private AtomicBoolean gotPreface = new AtomicBoolean(false);

        private void handleData(Http2Data frame, Stream stream) {
            // Only allowable if stream is open or half closed local
            switch(stream.getStatus()) {
                case OPEN:
                case HALF_CLOSED_LOCAL:
                    int payloadLength = http2Parser.getFrameLength(frame);
                    decrementIncomingWindow(frame.getStreamId(), payloadLength);
                    stream.checkAgainstContentLength(frame.getData().getReadableSize(), frame.isEndStream());

                    sideSpecificHandleData(frame, payloadLength, stream);

                    if(frame.isEndStream())
                        receivedEndStream(stream);
                    break;
                case HALF_CLOSED_REMOTE:
                    throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), stream.getStreamId(), Http2ErrorCode.STREAM_CLOSED, wrapperGen.emptyWrapper());
                case CLOSED:
                    throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), stream.getStreamId(), Http2ErrorCode.STREAM_CLOSED, wrapperGen.emptyWrapper());
                case IDLE:
                    throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), stream.getStreamId(), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());
                default:
                    throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
            }
        }

        private void handleHeaders(Http2Headers frame, Stream stream) {
            boolean isTrailer = false;
            switch (stream.getStatus()) {
                case IDLE:
                    long currentlyOpenStreams = countOpenRemoteOriginatedStreams();
                    log.info("got headers with currently open streams: " + currentlyOpenStreams);
                    if(localSettings.containsKey(SETTINGS_MAX_CONCURRENT_STREAMS) &&
                            currentlyOpenStreams >= localSettings.get(SETTINGS_MAX_CONCURRENT_STREAMS)) {
                        throw new RstStreamError(Http2ErrorCode.REFUSED_STREAM, stream.getStreamId());
                    }
                    stream.setStatus(Stream.StreamStatus.OPEN);
                    break;
                case HALF_CLOSED_LOCAL:
                    // No status change in this case
                    break;
                case RESERVED_REMOTE:
                    stream.setStatus(Stream.StreamStatus.HALF_CLOSED_LOCAL);
                    break;
                case OPEN:
                    if(!stream.isTrailerEnabled()) {
                        throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), stream.getStreamId(), Http2ErrorCode.STREAM_CLOSED, wrapperGen.emptyWrapper());
                    } else {
                        isTrailer = true;
                    }
                    break;
                default: // HALF_CLOSED_REMOTE, or CLOSED
                    throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), stream.getStreamId(), Http2ErrorCode.STREAM_CLOSED, wrapperGen.emptyWrapper());
            }

            if(frame.isPriority()) {
                stream.setPriorityDetails(frame.getPriorityDetails());
            }

            if(frame.isEndHeaders()) {
                // the parser has already accumulated the headers in the frame for us.

                boolean isComplete = frame.isEndStream();

                if(isTrailer) {
                    // Make sure that the headers match what we are expecting.
                    List<String> allowedTrailerHeaders = stream.getTrailerHeaders();
                    for(HasHeaderFragment.Header header: frame.getHeaderList()) {
                        if(!allowedTrailerHeaders.contains(header.header))
                            throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
                    }
                }

                // if we have no headers must be a compression error?
                if(frame.getHeaderList().isEmpty()) {
                    throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.COMPRESSION_ERROR, wrapperGen.emptyWrapper());
                }
                sideSpecificHandleHeaders(frame, isTrailer, stream);

                if (isComplete)
                    receivedEndStream(stream);

            }
            else {
                throw new InternalError(lastClosedRemoteOriginatedStream().orElse(0), wrapperGen.emptyWrapper());
            }
        }


        private void handlePriority(Http2Priority frame, Stream stream) {
            // Can be received in any state. We aren't doing anything with this right now.
            stream.setPriorityDetails(frame.getPriorityDetails());
        }

        private void handleRstStream(Http2RstStream frame, Stream stream) {
            switch(stream.getStatus()) {
                case OPEN:
                case HALF_CLOSED_REMOTE:
                case HALF_CLOSED_LOCAL:
                case RESERVED_LOCAL:
                case RESERVED_REMOTE:
                case CLOSED:
                    sideSpecificHandleRstStream(frame, stream);
                    stream.setStatus(CLOSED);
                    break;
                default:
                    throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());
            }
        }

        private void handlePushPromise(Http2PushPromise frame, Stream stream) {
            if(side == SERVER) {
                // Can't get pushpromise in the server
                throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());
            }

            // Can get this on any stream id, creates a new stream
            if(frame.isEndHeaders()) {
                long currentlyOpenStreams = countOpenRemoteOriginatedStreams();
                log.info("got push promise with currently open streams: " + currentlyOpenStreams);
                if(localSettings.containsKey(SETTINGS_MAX_CONCURRENT_STREAMS) &&
                        currentlyOpenStreams >= localSettings.get(SETTINGS_MAX_CONCURRENT_STREAMS)) {
                    throw new RstStreamError(Http2ErrorCode.REFUSED_STREAM, frame.getPromisedStreamId());
                }
                int newStreamId = frame.getPromisedStreamId();
                if(newStreamId <= lastIncomingStreamId.get()) {
                    throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());
                }
                lastIncomingStreamId.set(newStreamId);

                Stream promisedStream = new Stream();
                initializeFlowControl(newStreamId);
                promisedStream.setStreamId(newStreamId);

                // TODO: make sure streamid is valid
                // TODO: close all lower numbered even IDLE streams
                activeStreams.put(newStreamId, promisedStream);

                // Uses the same listener as the stream it came in on
                promisedStream.setResponseListener(stream.getResponseListener());
                HttpRequest request = requestFromHeaders(frame.getHeaderList(), promisedStream);
                promisedStream.setRequest(request);
                promisedStream.setStatus(Stream.StreamStatus.RESERVED_REMOTE);
            } else {
                throw new InternalError(lastClosedRemoteOriginatedStream().orElse(0), wrapperGen.emptyWrapper());
            }
        }

        private void handleWindowUpdate(Http2WindowUpdate frame, Stream stream) {
            if(frame.getWindowSizeIncrement() == 0) {
                throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());
            }

            incrementOutgoingWindow(frame.getStreamId(), frame.getWindowSizeIncrement());

            // clear all queues if the connection-level stream
            if(frame.getStreamId() == 0x0) {
                for (Map.Entry<Integer, ConcurrentLinkedDeque<PendingData>> entry : outgoingDataQueue.entrySet()) {
                    if (!entry.getValue().isEmpty())
                        clearQueue(entry.getKey());
                }
            }
            else {
                if(stream.getStatus() == IDLE) {
                    throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());
                }
                if(outgoingDataQueue.containsKey(frame.getStreamId())) {
                    clearQueue(frame.getStreamId());
                }
            }
        }

        private void handleSettings(Http2Settings frame) {
            if(frame.isAck()) {
                // we received an ack, so the settings we sent have been accepted.
                for(Map.Entry<Http2Settings.Parameter, Long> entry: localRequestedSettings.entrySet()) {
                    localSettings.put(entry.getKey(), entry.getValue());
                }
            } else {
                setRemoteSettings(frame, true);
            }
        }

        // TODO: actually deal with this goaway stuff where necessary
        private void handleGoAway(Http2GoAway frame) {
            remoteGoneAway = true;
            goneAwayLastStreamId = frame.getLastStreamId();
            goneAwayErrorCode = frame.getErrorCode();
            additionalDebugData = frame.getDebugData();
            farEndClosed(channel);
        }

        private void handlePing(Http2Ping frame) {
            if(!frame.isPingResponse()) {
                // Send the same frame back, setting ping response
                frame.setIsPingResponse(true);
                log.info("sending ping response: " + frame);
                channel.write(ByteBuffer.wrap(http2Parser.marshal(frame).createByteArray()));
            } else {
                // measure latency from the ping that was sent. The opaqueData we sent is
                // System.nanoTime() so we just measure the difference
                long latency = System.nanoTime() - frame.getOpaqueData();
                log.info("Ping: {} ms", latency * 1e-6);
            }
        }

        private void handleFrame(Http2Frame frame) {
            if(frame.getFrameType() != SETTINGS && !gotSettings.get()) {
                throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());
            }

            // Transition the stream state
            if(frame.getStreamId() != 0x0) {
                Stream stream = activeStreams.get(frame.getStreamId());

                // If the stream doesn't exist, create it, if server and if streamid is odd.
                if (stream == null) {
                    if (side == SERVER) {
                        int streamId = frame.getStreamId();
                        if(streamId <= lastIncomingStreamId.get() || frame.getStreamId() % 2 != 1) {
                            throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());
                        }
                        lastIncomingStreamId.set(streamId);
                        stream = new Stream();
                        stream.setStreamId(streamId);
                        initializeFlowControl(stream.getStreamId());
                        activeStreams.put(stream.getStreamId(), stream);
                    } else {
                        throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());
                    }
                }

                switch (frame.getFrameType()) {
                    case DATA:
                        handleData((Http2Data) frame, stream);
                        break;
                    case HEADERS:
                        handleHeaders((Http2Headers) frame, stream);
                        break;
                    case PRIORITY:
                        handlePriority((Http2Priority) frame, stream);
                        break;
                    case RST_STREAM:
                        handleRstStream((Http2RstStream) frame, stream);
                        break;
                    case PUSH_PROMISE:
                        handlePushPromise((Http2PushPromise) frame, stream);
                        break;
                    case WINDOW_UPDATE:
                        handleWindowUpdate((Http2WindowUpdate) frame, stream);
                        break;
                    case CONTINUATION:
                        throw new InternalError(lastClosedRemoteOriginatedStream().orElse(0), wrapperGen.emptyWrapper());
                    default:
                        throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), frame.getStreamId(), Http2ErrorCode.PROTOCOL_ERROR,
                                wrapperGen.emptyWrapper());
                }
            } else {
                switch (frame.getFrameType()) {
                    case WINDOW_UPDATE:
                        handleWindowUpdate((Http2WindowUpdate) frame, null);
                        break;
                    case SETTINGS:
                        handleSettings((Http2Settings) frame);
                        break;
                    case GOAWAY:
                        handleGoAway((Http2GoAway) frame);
                        break;
                    case PING:
                        handlePing((Http2Ping) frame);
                        break;
                    default:
                        throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR,
                                wrapperGen.emptyWrapper());
                }
            }
        }

        @Override
        public void incomingData(Channel channel, ByteBuffer b) {
            DataWrapper newData = wrapperGen.wrapByteBuffer(b);
            try {
                // TODO: turn the preface into a frame type.
                // First check to make sure we got our preface
                if(side == SERVER && !gotPreface.get()) {
                    // check to make sure we got the preface.
                    DataWrapper combined = wrapperGen.chainDataWrappers(oldData, newData);
                    int prefaceLength = prefaceHexString.length()/2;
                    if(combined.getReadableSize() >= prefaceLength) {
                        List<? extends DataWrapper> split = wrapperGen.split(combined, prefaceLength);
                        if(Arrays.equals(split.get(0).createByteArray(), (DatatypeConverter.parseHexBinary(prefaceHexString)))) {
                            gotPreface.set(true);
                            oldData = split.get(1);
                            log.info("got http2 preface");
                            sendLocalRequestedSettings();
                        } else {
                            throw new GoAwayError(0, Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());
                        }
                    } else {
                        oldData = combined;
                    }
                } else { // Either we got the preface or we don't need it
                    try {
                        ParserResult parserResult = http2Parser.parse(oldData, newData, decoder, localSettings);

                        for (Http2Frame frame : parserResult.getParsedFrames()) {
                            log.info("got frame=" + frame);
                            handleFrame(frame);
                        }
                        oldData = parserResult.getMoreData();
                    }
                    catch (ParseException e) {
                        if(e.isConnectionLevel()) {
                            if(e.hasStream()) {
                                throw new GoAwayError(lastClosedLocalOriginatedStream().orElse(0), e.getStreamId(), e.getErrorCode(), wrapperGen.emptyWrapper());
                            }
                            else {
                                throw new GoAwayError(lastClosedLocalOriginatedStream().orElse(0), e.getErrorCode(), wrapperGen.emptyWrapper());
                            }
                        } else {
                            throw new RstStreamError(e.getErrorCode(), e.getStreamId());
                        }
                    }
                }
            } catch (Http2Error e) {
                log.error("sending error frames " + e.toFrames(), e);
                channel.write(ByteBuffer.wrap(http2Parser.marshal(e.toFrames()).createByteArray()));
                if(RstStreamError.class.isInstance(e)) {
                    // Mark the stream closed
                    Stream stream = activeStreams.get(((RstStreamError) e).getStreamId());
                    if(stream != null)
                        stream.setStatus(CLOSED);
                }
                if(GoAwayError.class.isInstance(e)) {
                    // TODO: Shut this connection down properly.
                    channel.close();
                }
            }
        }

        @Override
        public void farEndClosed(Channel channel) {
            // TODO: deal with this
        }

        @Override
        public void failure(Channel channel, ByteBuffer data, Exception e) {
            // TODO: deal with this
        }

        @Override
        public void applyBackPressure(Channel channel) {

        }

        @Override
        public void releaseBackPressure(Channel channel) {

        }
    }
}
