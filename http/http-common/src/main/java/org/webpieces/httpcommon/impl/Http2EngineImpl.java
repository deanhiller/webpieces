package org.webpieces.httpcommon.impl;

import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.HEADERS;
import static com.webpieces.http2parser.api.dto.lib.Http2FrameType.PUSH_PROMISE;
import static com.webpieces.http2parser.api.dto.lib.SettingsParameter.SETTINGS_ENABLE_PUSH;
import static com.webpieces.http2parser.api.dto.lib.SettingsParameter.SETTINGS_HEADER_TABLE_SIZE;
import static com.webpieces.http2parser.api.dto.lib.SettingsParameter.SETTINGS_INITIAL_WINDOW_SIZE;
import static com.webpieces.http2parser.api.dto.lib.SettingsParameter.SETTINGS_MAX_FRAME_SIZE;
import static java.lang.Math.min;
import static org.webpieces.httpcommon.api.Http2Engine.HttpSide.CLIENT;
import static org.webpieces.httpcommon.api.Http2Engine.HttpSide.SERVER;
import static org.webpieces.httpcommon.impl.Stream.StreamStatus.CLOSED;
import static org.webpieces.httpcommon.impl.Stream.StreamStatus.HALF_CLOSED_REMOTE;
import static org.webpieces.httpparser.api.common.KnownHeaderName.TRAILER;
import static org.webpieces.httpparser.api.dto.HttpRequest.HttpScheme.HTTPS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntUnaryOperator;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpcommon.api.Http2Engine;
import org.webpieces.httpcommon.api.exceptions.ClientError;
import org.webpieces.httpcommon.api.exceptions.GoAwayError;
import org.webpieces.httpcommon.api.exceptions.InternalError;
import org.webpieces.httpcommon.api.exceptions.RstStreamError;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.Headers;
import org.webpieces.httpparser.api.dto.HttpMessage;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpRequestMethod;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpResponseStatus;
import org.webpieces.httpparser.api.dto.HttpResponseStatusLine;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.HttpVersion;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.httpparser.api.dto.UrlInfo;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2SettingsMap;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.HeadersFrame;
import com.webpieces.http2parser.api.dto.PingFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.WindowUpdateFrame;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;

public abstract class Http2EngineImpl implements Http2Engine {
    static final Logger log = LoggerFactory.getLogger(Http2EngineImpl.class);
    static DataWrapperGenerator wrapperGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    static String prefaceHexString = "505249202a20485454502f322e300d0a0d0a534d0d0a0d0a";

    Channel channel;
    DataListener dataListener;
    Http2Parser http2Parser;
    private InetSocketAddress remoteAddress;

    HttpSide side;

    Http2SettingsMap localRequestedSettings;

    // remotesettings doesn't need concurrent bc listener is vts
    Http2SettingsMap remoteSettings = new Http2SettingsMap();
    AtomicBoolean gotSettings = new AtomicBoolean(false);
    CountDownLatch settingsLatch = new CountDownLatch(1);

    // localsettings also doesn't need concurrent bc local settings is only set when
    // it gets the ack from the settings that gets sent.
    Http2SettingsMap localSettings = new Http2SettingsMap();

    ConcurrentHashMap<Integer, Stream> activeStreams = new ConcurrentHashMap<>();
    private AtomicInteger nextOutgoingStreamId;
    AtomicInteger lastIncomingStreamId = new AtomicInteger(0);
    private ConcurrentHashMap<Integer, AtomicLong> outgoingFlowControl = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, AtomicLong> incomingFlowControl = new ConcurrentHashMap<>();
    class PendingData {
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

    ConcurrentHashMap<Integer, ConcurrentLinkedDeque<PendingData>> outgoingDataQueue = new ConcurrentHashMap<>();

    private Encoder encoder;
    Decoder decoder;
    private AtomicBoolean maxHeaderTableSizeNeedsUpdate = new AtomicBoolean(false);
    private AtomicInteger minimumMaxHeaderTableSizeUpdate = new AtomicInteger(Integer.MAX_VALUE);

    // TODO: figure out how to deal with the goaway. For now we're just
    // going to record what they told us.
    // these don't have to be concurrent-safe because the datalistener is virtually single threaded.
    boolean remoteGoneAway = false;
    int goneAwayLastStreamId;
    Http2ErrorCode goneAwayErrorCode;
    DataWrapper additionalDebugData;


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

        this.dataListener = new Http2DataListener(this);

        initializeFlowControl(0x0);
    }

    @Override
    public Channel getUnderlyingChannel() {
        return channel;
    }


    @Override
    public void sendLocalRequestedSettings() {
        SettingsFrame settingsFrame = new SettingsFrame();

        localRequestedSettings.fillFrame(settingsFrame);
        log.info("sending settings: " + settingsFrame);
        channel.write(ByteBuffer.wrap(http2Parser.marshal(settingsFrame).createByteArray()));
    }

    void setRemoteSettings(SettingsFrame frame, boolean sendAck) {
    	
    	Http2SettingsMap setMap = new Http2SettingsMap(frame.getSettings());
        // We've received a settings. Check for legit-ness.
        if(setMap.get(SETTINGS_ENABLE_PUSH) != null && (
                setMap.get(SETTINGS_ENABLE_PUSH) != 0 && setMap.get(SETTINGS_ENABLE_PUSH) != 1))
            throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());

        // 2^31 - 1 - max flow control window
        if(setMap.get(SETTINGS_INITIAL_WINDOW_SIZE) != null &&
                setMap.get(SETTINGS_INITIAL_WINDOW_SIZE) > 2147483647)
            throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.FLOW_CONTROL_ERROR, wrapperGen.emptyWrapper());

        // frame size must be between 16384 and 2^24 - 1
        if(setMap.get(SETTINGS_MAX_FRAME_SIZE) != null && (
                setMap.get(SETTINGS_MAX_FRAME_SIZE) < 16384 || setMap.get(SETTINGS_MAX_FRAME_SIZE) > 1677215))
            throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());

        // Update remoteSettings
        log.info("Setting remote settings to: " + setMap);
        gotSettings.set(true);
        settingsLatch.countDown();

        for(Map.Entry<SettingsParameter, Long> entry: setMap.entrySet()) {
            remoteSettings.put(entry.getKey(), entry.getValue());
        }

        // What do we do when certain settings are updated
        if(setMap.containsKey(SETTINGS_HEADER_TABLE_SIZE)) {
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
                    new UpdateMinimum(setMap.get(SETTINGS_HEADER_TABLE_SIZE).intValue()));
        }
        if(sendAck) {
            SettingsFrame responseFrame = new SettingsFrame();
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

    SettingsFrame getLocalRequestedSettingsFrame() {
        SettingsFrame settingsFrame = new SettingsFrame();
        localRequestedSettings.fillFrame(settingsFrame);
        return settingsFrame;
    }

    LinkedList<Http2Header> requestToHeaders(HttpRequest request) {
        HttpRequestLine requestLine = request.getRequestLine();
        List<Header> requestHeaders = request.getHeaders();

        LinkedList<Http2Header> headerList = new LinkedList<>();

        // add special headers
        headerList.add(new Http2Header(":method", requestLine.getMethod().getMethodAsString()));

        UrlInfo urlInfo = requestLine.getUri().getUriBreakdown();
        headerList.add(new Http2Header(":path", urlInfo.getFullPath()));

        // Figure out scheme
        if(urlInfo.getPrefix() != null) {
            headerList.add(new Http2Header(":scheme", urlInfo.getPrefix()));
        } else {
            if(channel.isSslChannel()) {
                headerList.add(new Http2Header(":scheme", "https"));
            } else {
                headerList.add(new Http2Header(":scheme", "http"));
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
            headerList.add(new Http2Header(":authority", h));
        } else {
            if (urlInfo.getHost() != null) {
                if (urlInfo.getPort() == null)
                    headerList.add(new Http2Header(":authority", urlInfo.getHost()));
                else
                    headerList.add(new Http2Header(":authority", String.format("%s:%d", urlInfo.getHost(), urlInfo.getPort())));
            } else {
                headerList.add(new Http2Header(":authority", remoteAddress.getHostName() + ":" + remoteAddress.getPort()));
            }
        }

        // Add regular headers
        for(Header header: requestHeaders) {
            headerList.add(new Http2Header(header.getName().toLowerCase(), header.getValue()));
        }

        return headerList;
    }


    void clearQueue(int streamId) {
        ConcurrentLinkedDeque<PendingData> queue = outgoingDataQueue.get(streamId);
        while (queue != null && !queue.isEmpty() && outgoingFlowControl.get(streamId).get() > 0) {
            PendingData pendingData = queue.poll();
            log.info("sending data from the queue: " + pendingData.data);
            sendDataFrames(pendingData.data, pendingData.isComplete, pendingData.stream, true)
                    .thenAccept(v -> pendingData.future.complete(null));
        }
    }

    private CompletableFuture<Void> writeDataFrame(DataFrame frame) {
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
                int dataLength = body.getReadableSize();

                // If there's things on the queue and we didn't get this from the front of the queue,
                // queue this data.
                if(!isQueueEmpty(stream) && !wasFrontOfQueue) {
                    log.info("queueing data: " + body);
                    return queueData(body, isComplete, stream, false);
                }

                long maxLength = min(remoteSettings.get(SETTINGS_MAX_FRAME_SIZE),
                        min(outgoingFlowControl.get(stream.getStreamId()).get(),
                                outgoingFlowControl.get(0x0).get()));


                DataFrame newFrame = new DataFrame();
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
    CompletableFuture<Void> sendPushPromiseFrames(LinkedList<Http2Header> headerList, Stream stream, Stream newStream) {
        int streamId = stream.getStreamId();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        updateMaxHeaderTableSize(out);

        List<AbstractHttp2Frame> frameList = http2Parser.createHeaderFrames(headerList, PUSH_PROMISE, newStream.getStreamId(), remoteSettings, encoder, out);
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
    CompletableFuture<Void> sendHeaderFrames(LinkedList<Http2Header> headerList, Stream stream) {
        // TODO: check the status of the stream to ensure we can send HEADER frames

        int streamId = stream.getStreamId();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        updateMaxHeaderTableSize(out);

        List<AbstractHttp2Frame> frameList = http2Parser.createHeaderFrames(headerList, HEADERS, streamId, remoteSettings, encoder, out);

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
    long countOpenRemoteOriginatedStreams() {
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

    Optional<Integer> lastClosedRemoteOriginatedStream() {
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
            PingFrame pingFrame = new PingFrame();
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

    void decrementIncomingWindow(int streamId, int length) {
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

    void incrementOutgoingWindow(int streamId, int length) {
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

        WindowUpdateFrame frame = new WindowUpdateFrame();
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

    LinkedList<Http2Header> responseToHeaders(HttpResponse response) {
        LinkedList<Http2Header> headers = new LinkedList<>();
        headers.add(new Http2Header(":status", response.getStatusLine().getStatus().getCode().toString()));
        for(Header header: response.getHeaders()) {
            headers.add(new Http2Header(header.getName(), header.getValue()));
        }
        return headers;
    }

    abstract void sideSpecificHandleData(DataFrame frame, int payloadLength, Stream stream);

    abstract void sideSpecificHandleHeaders(HeadersFrame frame, boolean isTrailer, Stream stream);

    abstract void sideSpecificHandleRstStream(RstStreamFrame frame, Stream stream);

    void receivedEndStream(Stream stream) {
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

    private Map<String, String> processSpecialHeaders(Queue<Http2Header> headers,
                                                      List<String> specialHeaders,
                                                      HttpMessage msg,
                                                      int streamId) {
        Map<String, String> specialHeaderMap = new HashMap<>();

        boolean processingSpecialHeaders = true;
        for(Http2Header header: headers) {
            if(processingSpecialHeaders) {
                if(!header.getName().startsWith((":")))
                {
                    processingSpecialHeaders = false;
                } else {
                    // If we got a special header twice, or a special header we are not expecting
                    if (specialHeaderMap.get(header.getName()) != null || !specialHeaders.contains(header.getName())) {
                        throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, streamId);
                    }
                    specialHeaderMap.put(header.getName(), header.getValue());
                }
            }
            if(!processingSpecialHeaders) {
                // if we got a special header mixed in with the regular headers
                if(header.getName().startsWith(":")) {
                    throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, streamId);
                }
                msg.addHeader(new Header(header.getName(), header.getValue()));
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

    HttpResponse responseFromHeaders(Queue<Http2Header> headers, Stream stream) {
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

    HttpRequest requestFromHeaders(Queue<Http2Header> headers, Stream stream) {
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
}
