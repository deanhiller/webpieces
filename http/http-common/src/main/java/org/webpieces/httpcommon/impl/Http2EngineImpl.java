package org.webpieces.httpcommon.impl;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.webpieces.http2parser.api.Http2Parser;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
import static org.webpieces.httpparser.api.dto.HttpRequest.HttpScheme.HTTPS;

public class Http2EngineImpl implements Http2Engine {
    private static final Logger log = LoggerFactory.getLogger(Http2EngineImpl.class);
    private static DataWrapperGenerator wrapperGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    private static String prefaceHexString = "505249202a20485454502f322e300d0a0d0a534d0d0a0d0a";

    private Channel channel;
    private DataListener dataListener;
    private Http2Parser http2Parser;
    private InetSocketAddress remoteAddress;

    private HttpSide side;

    private Map<Http2Settings.Parameter, Integer> localPreferredSettings = new HashMap<>();

    // remotesettings doesn't need concurrent bc listener is vts
    private Map<Http2Settings.Parameter, Integer> remoteSettings = new HashMap<>();
    private AtomicBoolean gotSettings = new AtomicBoolean(false);

    // localsettings also doesn't need concurrent bc local settigs is only set when
    // it gets the ack from the settings that gets sent.
    private Map<Http2Settings.Parameter, Integer> localSettings = new HashMap<>();

    private ConcurrentHashMap<Integer, Stream> activeStreams = new ConcurrentHashMap<>();
    private AtomicInteger nextStreamId;
    private ConcurrentHashMap<Integer, AtomicInteger> outgoingFlowControl = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, AtomicInteger> incomingFlowControl = new ConcurrentHashMap<>();
    private class PendingDataFrame {
        CompletableFuture<Channel> future;
        Http2Data frame;

        PendingDataFrame(CompletableFuture<Channel> future, Http2Data frame) {
            this.future = future;
            this.frame = frame;
        }
    }

    private ConcurrentHashMap<Integer, ConcurrentLinkedQueue<PendingDataFrame>> outgoingDataQueue = new ConcurrentHashMap<>();

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

    // Server only
    private RequestListener requestListener;
    private ResponseSender responseSender = new Http2ResponseSender(this);


    // Server only
    @Override
    public ResponseSender getResponseSender() {
        return responseSender;
    }

    // Server only
    @Override
    public void setRequestListener(RequestListener requestListener) {
        this.requestListener = requestListener;
    }

    public Http2EngineImpl(Http2Parser http2Parser, Channel channel, InetSocketAddress remoteAddress, HttpSide side) {
        this.http2Parser = http2Parser;
        this.channel = channel;
        this.remoteAddress = remoteAddress;
        this.side = side;
        if(side == CLIENT) {
            this.nextStreamId = new AtomicInteger(0x1);
        }
        else {
            this.nextStreamId = new AtomicInteger(0x2);
        }

        // Initialize to defaults
        remoteSettings.put(SETTINGS_HEADER_TABLE_SIZE, 4096);
        localSettings.put(SETTINGS_HEADER_TABLE_SIZE, 4096);

        remoteSettings.put(SETTINGS_ENABLE_PUSH, 1);
        localSettings.put(SETTINGS_ENABLE_PUSH, 1);

        // No limit for MAX_CONCURRENT_STREAMS by default so it isn't in the map

        remoteSettings.put(SETTINGS_INITIAL_WINDOW_SIZE, 65535);
        localSettings.put(SETTINGS_INITIAL_WINDOW_SIZE, 65535);

        remoteSettings.put(SETTINGS_MAX_FRAME_SIZE, 16384);
        localSettings.put(SETTINGS_MAX_FRAME_SIZE, 16384);

        // No limit for MAX_HEADER_LIST_SIZE by default, so not in the map

        this.decoder = new Decoder(4096, localSettings.get(SETTINGS_HEADER_TABLE_SIZE));
        this.encoder = new Encoder(remoteSettings.get(SETTINGS_HEADER_TABLE_SIZE));

        this.dataListener = new Http2DataListener();

        initializeFlowControl(0x0);
    }

    public Channel getUnderlyingChannel() {
        return channel;
    }

    // Client only
    public void sendHttp2Preface() {
        log.info("sending preface");
        channel.write(ByteBuffer.wrap(DatatypeConverter.parseHexBinary(prefaceHexString)));
    }

    public void sendLocalPreferredSettings() {
        Http2Settings settingsFrame = new Http2Settings();

        settingsFrame.setSettings(localPreferredSettings);
        log.info("sending settings: " + settingsFrame);
        channel.write(ByteBuffer.wrap(http2Parser.marshal(settingsFrame).createByteArray()));
    }

    @Override
    public void setRemoteSettings(Http2Settings frame) {
        // We've received a settings. Update remoteSettings and send an ack
        gotSettings.set(true);
        for(Map.Entry<Http2Settings.Parameter, Integer> entry: frame.getSettings().entrySet()) {
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
                    new UpdateMinimum(frame.getSettings().get(SETTINGS_HEADER_TABLE_SIZE)));
        }
        Http2Settings responseFrame = new Http2Settings();
        responseFrame.setAck(true);
        log.info("sending settings ack: " + responseFrame);
        channel.write(ByteBuffer.wrap(http2Parser.marshal(responseFrame).createByteArray()));
    }

    public void initialize() {

        Timer timer = new Timer();
        // in 5 seconds send a ping every 5 seconds
        timer.schedule(new SendPing(), 5000, 5000);
    }

    public DataListener getDataListener() {
        return dataListener;
    }

    public Http2Settings getLocalRequestedSettingsFrame() {
        Http2Settings settingsFrame = new Http2Settings();
        settingsFrame.setSettings(localPreferredSettings);
        return settingsFrame;
    }

    // Client only
    @Override
    public RequestId createInitialStream(HttpResponse r, HttpRequest req, ResponseListener listener, DataWrapper leftOverData) {
        if(side != CLIENT) throw new RuntimeException("can't call createInitialStream from server");

        int initialStreamId = getAndIncrementStreamId();
        Stream initialStream = new Stream();
        initialStream.setStreamId(initialStreamId);
        initializeFlowControl(initialStreamId);
        initialStream.setRequest(req);
        initialStream.setResponseListener(listener);
        initialStream.setResponse(r);
        // Since we already sent the entire request as the upgrade, the stream basically starts in
        // half closed local
        initialStream.setStatus(Stream.StreamStatus.HALF_CLOSED_LOCAL);
        activeStreams.put(initialStreamId, initialStream);

        DataWrapper responseBody = r.getBodyNonNull();

        // Send the content of the response to the datalistener, if any
        // Not likely to happen but just in case
        if(responseBody.getReadableSize() > 0)
            dataListener.incomingData(channel, ByteBuffer.wrap(responseBody.createByteArray()));

        if(leftOverData.getReadableSize() > 0)
            dataListener.incomingData(channel, ByteBuffer.wrap(leftOverData.createByteArray()));

        return new RequestId(initialStreamId);
    }

    @Override
    public CompletableFuture<Void> sendData(RequestId id, DataWrapper data, boolean isComplete) {
        Stream stream = activeStreams.get(id.getValue());
        return sendDataFrames(data, isComplete, stream);
    }

    private LinkedList<HasHeaderFragment.Header> requestToHeaders(HttpRequest request) {
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


    private CompletableFuture<Channel> actuallyWriteDataFrame(PendingDataFrame pendingDataFrame) {
        Http2Data frame = pendingDataFrame.frame;
        CompletableFuture<Channel> future = pendingDataFrame.future;

        int streamId = frame.getStreamId();
        decrementOutgoingWindow(streamId, http2Parser.getFrameLength(frame));
        log.info("actually writing data frame: " + frame);
        return channel.write(ByteBuffer.wrap(http2Parser.marshal(frame).createByteArray())).thenApply(
                channel -> {
                    future.complete(channel);
                    return channel;
                }
        );
    }

    private boolean canSendFrame(Http2Data dataFrame) {
        int length = http2Parser.getFrameLength(dataFrame);
        return (outgoingFlowControl.get(dataFrame.getStreamId()).get() > length &&
                outgoingFlowControl.get(0x0).get() > length);
    }

    private void clearQueue(int streamId) {
        ConcurrentLinkedQueue<PendingDataFrame> queue = outgoingDataQueue.get(streamId);
        if (!queue.isEmpty()) {
            synchronized (queue) {
                PendingDataFrame pendingDataFrame = queue.peek();
                if(pendingDataFrame != null) {
                    int length = http2Parser.getFrameLength(pendingDataFrame.frame);
                    if (outgoingFlowControl.get(streamId).get() > length &&
                            outgoingFlowControl.get(0x0).get() > length) {
                        // Will write one frame, then write more frames
                        queue.poll();
                        actuallyWriteDataFrame(pendingDataFrame).thenAccept(channel -> clearQueue(streamId));
                    }
                }
            }
        }
    }

    private CompletableFuture<Channel> writeDataFrame(Http2Data dataFrame) {
        int streamId = dataFrame.getStreamId();
        if(!outgoingDataQueue.contains(streamId)) {
            outgoingDataQueue.put(streamId, new ConcurrentLinkedQueue<>());
        }
        CompletableFuture<Channel> future = new CompletableFuture<>();
        PendingDataFrame pendingDataFrame = new PendingDataFrame(future, dataFrame);

        if(canSendFrame(dataFrame) && outgoingDataQueue.get(streamId).isEmpty()) {
            return actuallyWriteDataFrame(pendingDataFrame);
        } else {
            log.info("adding frame to the queue:" + pendingDataFrame);
            outgoingDataQueue.get(streamId).add(pendingDataFrame);
            return future;
        }
    }

    private CompletableFuture<Void> sendDataFrames(DataWrapper body, boolean isComplete, Stream stream) {
        switch(stream.getStatus()) {
            case OPEN:
            case HALF_CLOSED_REMOTE:
                Http2Data newFrame = new Http2Data();
                newFrame.setStreamId(stream.getStreamId());

                // writes only one frame at a time.
                if(body.getReadableSize() <= remoteSettings.get(SETTINGS_MAX_FRAME_SIZE)) {
                    // the body fits within one frame so send an endstream with this frame
                    newFrame.setData(body);
                    if(isComplete)
                        newFrame.setEndStream(true);

                    log.info("queuing final data frame: (but might not complete the request)" + newFrame);
                    return writeDataFrame(newFrame).thenAccept(
                            channel -> {
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
                    // to big, split it, send, and recurse.
                    List<? extends DataWrapper> split = wrapperGen.split(body, remoteSettings.get(SETTINGS_MAX_FRAME_SIZE));
                    newFrame.setData(split.get(0));
                    log.info("queuing non-final data frame: " + newFrame);
                    return writeDataFrame(newFrame).thenCompose(
                            channel ->  sendDataFrames(split.get(1), isComplete, stream)
                    );
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
                int newMaxHeaderTableSize = remoteSettings.get(SETTINGS_HEADER_TABLE_SIZE);
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
    private CompletableFuture<Void> sendPushPromiseFrames(LinkedList<HasHeaderFragment.Header> headerList, Stream stream, Stream newStream) {
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
    private CompletableFuture<Void> sendHeaderFrames(LinkedList<HasHeaderFragment.Header> headerList, Stream stream) {
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

    private long countOpenLocalOriginatedStreams() {
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

    private int getAndIncrementStreamId() {
        return nextStreamId.getAndAdd(2);
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
        outgoingFlowControl.get(0x0).addAndGet(length);
        outgoingFlowControl.get(streamId).addAndGet(length);
        log.info("stream {} outgoing window is {} and connection window is {}", streamId, outgoingFlowControl.get(streamId), outgoingFlowControl.get(0));
    }

    private void incrementIncomingWindow(int streamId, int length) {
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

    private void initializeFlowControl(int streamId) {
        // Set up flow control
        incomingFlowControl.put(streamId, new AtomicInteger(localSettings.get(SETTINGS_INITIAL_WINDOW_SIZE)));
        outgoingFlowControl.put(streamId, new AtomicInteger(remoteSettings.get(SETTINGS_INITIAL_WINDOW_SIZE)));
    }

    public void cleanUpPendings(String msg) {
        // TODO: deal with http2 streams to be cleaned up
    }

    // Client only
    public CompletableFuture<RequestId> sendRequest(HttpRequest request, boolean isComplete, ResponseListener l) {

        // Check if we are allowed to create a new stream
        if (remoteSettings.containsKey(SETTINGS_MAX_CONCURRENT_STREAMS) &&
                countOpenLocalOriginatedStreams() >= remoteSettings.get(SETTINGS_MAX_CONCURRENT_STREAMS)) {
            throw new ClientError("Max concurrent streams exceeded, please wait and try again.");
            // TODO: create a request queue that gets emptied when there are open streams
        }
        // Create a stream
        Stream newStream = new Stream();

        // Find a new Stream id
        int thisStreamId = getAndIncrementStreamId();
        newStream.setResponseListener(l);
        newStream.setStreamId(thisStreamId);
        newStream.setRequest(request);
        initializeFlowControl(thisStreamId);
        activeStreams.put(thisStreamId, newStream);
        LinkedList<HasHeaderFragment.Header> headers = requestToHeaders(request);
        return sendHeaderFrames(headers, newStream)
                .thenCompose(
                        channel -> sendDataFrames(request.getBodyNonNull(), isComplete, newStream))
                .thenApply(channel -> new RequestId(thisStreamId));

    }

    private LinkedList<HasHeaderFragment.Header> responseToHeaders(HttpResponse response) {
        LinkedList<HasHeaderFragment.Header> headers = new LinkedList<>();
        headers.add(new HasHeaderFragment.Header(":status", response.getStatusLine().getStatus().getCode().toString()));
        for(Header header: response.getHeaders()) {
            headers.add(new HasHeaderFragment.Header(header.getName(), header.getValue()));
        }
        return headers;
    }

    // Server only
    @Override
    public CompletableFuture<ResponseId> sendResponse(HttpResponse response, HttpRequest request, RequestId requestId, boolean isComplete) {
        Stream responseStream = activeStreams.get(requestId.getValue());

        if(responseStream == null && requestId.getValue() == 0x1) {
            if(requestId.getValue() == 0x1) {
                // Create a new response stream for this stream, because this is the first response after an upgrade
                responseStream = new Stream();
                responseStream.setStreamId(0x1);
                responseStream.setRequest(request);
                initializeFlowControl(0x1);
                responseStream.setStatus(Stream.StreamStatus.HALF_CLOSED_REMOTE);
                activeStreams.put(0x1, responseStream);
            }
            else {
                throw new RuntimeException("invalid request id " + requestId);
            }
        }
        // If we already have a response stored in the responseStream then we've already sent a response for this
        // stream and we need to send a push promise and create a new stream
        if(responseStream.getResponse() != null) { // If push promise, do some stuff (send PUSH_PROMISE frames, set up the new stream id, etc)
            if(remoteSettings.get(SETTINGS_ENABLE_PUSH) == 0) {
               // Enable push is not permitted, so ignore this response and return a -1 response id
                log.info("push promise not permitted by client, ignoring pushed response");
                return CompletableFuture.completedFuture(new ResponseId(0));
            }
            if(remoteSettings.get(SETTINGS_MAX_CONCURRENT_STREAMS) != null &&
                    countOpenLocalOriginatedStreams() > remoteSettings.get(SETTINGS_MAX_CONCURRENT_STREAMS)) {
                // Too many open streams already, so going to drop this push promise
                log.info("max concurrent streams exceeded, ignoring pushed response");
                return CompletableFuture.completedFuture(new ResponseId(0));
            }
            Stream newStream = new Stream();
            newStream.setStreamId(getAndIncrementStreamId());
            newStream.setResponse(response);
            newStream.setRequest(request);
            initializeFlowControl(newStream.getStreamId());
            activeStreams.put(newStream.getStreamId(), newStream);

            return sendPushPromiseFrames(requestToHeaders(request), responseStream, newStream)
                    .thenCompose(v -> actuallySendResponse(response, newStream, isComplete));
        }
        else {
            responseStream.setResponse(response);
            return actuallySendResponse(response, responseStream, isComplete);
        }

    }

    private CompletableFuture<ResponseId> actuallySendResponse(HttpResponse response, Stream stream, boolean isComplete) {
        return sendHeaderFrames(responseToHeaders(response), stream)
                .thenAccept(v -> sendDataFrames(response.getBodyNonNull(), isComplete, stream))
                .thenApply(v -> stream.getResponseId()).exceptionally(e -> {
                    log.error("can't send header frames", e);
                    return stream.getResponseId();
                });
    }

    // Server only
    @Override
    public CompletableFuture<Void> sendData(DataWrapper data, ResponseId responseId, boolean isComplete) {
        // If the responseid is 0 then we had just rejected the sendresponse because push promise is not permitted
        if(responseId.getValue() == 0) {
            log.info("push promise will be rejected by client, ignoring pushed data");
            return CompletableFuture.completedFuture(null);
        }
        Stream responseStream = activeStreams.get(responseId.getValue());
        if(responseStream == null) {
            // TODO: use the right exception here
            throw new RuntimeException("invalid responseid: " + responseId);
        }
        return sendDataFrames(data, isComplete, responseStream);
    }

    private class Http2DataListener implements DataListener {
        private DataWrapper oldData = http2Parser.prepareToParse();
        private AtomicBoolean gotPreface = new AtomicBoolean(false);

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

        private void handleData(Http2Data frame, Stream stream) {
            // Only allowable if stream is open or half closed local
            switch(stream.getStatus()) {
                case OPEN:
                case HALF_CLOSED_LOCAL:
                    boolean isComplete = frame.isEndStream();
                    int payloadLength = http2Parser.getFrameLength(frame);
                    decrementIncomingWindow(frame.getStreamId(), payloadLength);
                    if(side == CLIENT)
                        stream.getResponseListener().incomingData(frame.getData(), stream.getResponseId(), isComplete).thenAccept(
                                length -> incrementIncomingWindow(frame.getStreamId(), payloadLength));
                    else
                        requestListener.incomingData(frame.getData(), stream.getRequestId(), isComplete, responseSender);
                    if(isComplete)
                        receivedEndStream(stream);
                    break;
                default:
                    throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
            }
        }

        private HttpResponse responseFromHeaders(Queue<HasHeaderFragment.Header> headers, Stream stream) {
            HttpResponse response = new HttpResponse();

            // TODO: throw if special headers are not at the front, or we get a bad special header
            // Set special header
            String statusString = null;
            for(HasHeaderFragment.Header header: headers) {
                if (header.header.equals(":status")) {
                    statusString = header.value;
                    break;
                }
            }
            if(statusString == null)
                throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());

            HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
            HttpResponseStatus status = new HttpResponseStatus();
            try {
                status.setKnownStatus(KnownStatusCode.lookup(Integer.parseInt(statusString)));
            } catch(NumberFormatException e) {
                throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
            }

            statusLine.setStatus(status);
            HttpVersion version = new HttpVersion();
            version.setVersion("2.0");
            statusLine.setVersion(version);

            response.setStatusLine(statusLine);

            // Set all other headers
            for(HasHeaderFragment.Header header: headers) {
                if(!header.header.equals(":status"))
                    response.addHeader(new Header(header.header, header.value));
            }

            return response;
        }

        private HttpRequest requestFromHeaders(Queue<HasHeaderFragment.Header> headers, Stream stream) {
            Map<String, String> headerMap = new HashMap<>();
            for(HasHeaderFragment.Header header: headers) {
                headerMap.put(header.header, header.value);
            }
            HttpRequest request = new HttpRequest();

            // Set special headers
            // TODO: throw if special headers are not at the front, or we get a bad special header
            String method = headerMap.get(":method");
            String scheme = headerMap.get(":scheme");
            String authority = headerMap.get(":authority");
            String path = headerMap.get(":path");
            if(method == null || scheme == null || authority == null || path == null)
                throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());

            // See https://svn.tools.ietf.org/svn/wg/httpbis/specs/rfc7230.html#asterisk-form
            if(method.toLowerCase().equals("options") && path.equals("*")) {
                path = "";
            }

            HttpRequestLine requestLine = new HttpRequestLine();
            requestLine.setUri(new HttpUri(String.format("%s://%s%s", scheme, authority, path)));
            switch(scheme.toLowerCase()) {
                case "http":
                    request.setHttpScheme(HttpRequest.HttpScheme.HTTP);
                    break;
                case "https":
                    request.setHttpScheme(HTTPS);
                    break;
                default:
                    throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
            }

            requestLine.setMethod(new HttpRequestMethod(method));
            HttpVersion version = new HttpVersion();
            version.setVersion("2.0");
            requestLine.setVersion(version);
            request.setRequestLine(requestLine);

            List<String> specialHeaders = Arrays.asList(":method", ":scheme", ":authority", ":path");

            // Set all other headers
            // TODO: throw if there is an additional special header
            for(HasHeaderFragment.Header header: headers) {
                if(!specialHeaders.contains(header.header))
                    request.addHeader(new Header(header.header, header.value));
            }

            return request;
        }

        private void handleHeaders(Http2Headers frame, Stream stream) {
            switch (stream.getStatus()) {
                case IDLE:
                    stream.setStatus(Stream.StreamStatus.OPEN);
                    break;
                case HALF_CLOSED_LOCAL:
                    // No status change in this case
                    break;
                case RESERVED_REMOTE:
                    stream.setStatus(Stream.StreamStatus.HALF_CLOSED_LOCAL);
                    break;
                default:
                    throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
            }

            if(frame.isPriority()) {
                stream.setPriorityDetails(frame.getPriorityDetails());
            }

            if(frame.isEndHeaders()) {
                // the parser has already accumulated the headers in the frame for us.

                boolean isComplete = frame.isEndStream();

                if(side == CLIENT) {
                    HttpResponse response = responseFromHeaders(frame.getHeaderList(), stream);
                    stream.setResponse(response);
                    stream.getResponseListener().incomingResponse(response, stream.getRequest(), stream.getResponseId(), isComplete);
                } else {
                    HttpRequest request = requestFromHeaders(frame.getHeaderList(), stream);
                    stream.setRequest(request);
                    requestListener.incomingRequest(request, stream.getRequestId(), isComplete, responseSender);
                }

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
                    if(side == CLIENT)
                        stream.getResponseListener().failure(new RstStreamError(frame.getErrorCode(), stream.getStreamId()));
                    else
                        // TODO: change incomingError to failure and fix the exception types
                        responseSender.sendException(null);

                    stream.setStatus(CLOSED);
                    break;
                default:
                    throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
            }
        }

        private void handlePushPromise(Http2PushPromise frame, Stream stream) {
            if(side == SERVER) {
                // Can't get pushpromise in the server
                throw new RstStreamError(Http2ErrorCode.REFUSED_STREAM, frame.getPromisedStreamId());
            }

            // Can get this on any stream id, creates a new stream
            if(frame.isEndHeaders()) {
                if(localSettings.containsKey(SETTINGS_MAX_CONCURRENT_STREAMS) &&
                        countOpenRemoteOriginatedStreams() >= localSettings.get(SETTINGS_MAX_CONCURRENT_STREAMS)) {
                    throw new RstStreamError(Http2ErrorCode.REFUSED_STREAM, frame.getPromisedStreamId());
                }
                Stream promisedStream = new Stream();
                int newStreamId = frame.getPromisedStreamId();
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
            incrementOutgoingWindow(stream.getStreamId(), frame.getWindowSizeIncrement());

            // clear all queues if the connection-level stream
            if(frame.getStreamId() == 0x0) {
                for (Map.Entry<Integer, ConcurrentLinkedQueue<PendingDataFrame>> entry : outgoingDataQueue.entrySet()) {
                    if (!entry.getValue().isEmpty())
                        clearQueue(entry.getKey());
                }
            }
            else {
                if(outgoingDataQueue.containsKey(frame.getStreamId())) {
                    clearQueue(frame.getStreamId());
                }
            }
        }

        private void handleSettings(Http2Settings frame) {
            if(frame.isAck()) {
                // we received an ack, so the settings we sent have been accepted.
                for(Map.Entry<Http2Settings.Parameter, Integer> entry: localPreferredSettings.entrySet()) {
                    localSettings.put(entry.getKey(), entry.getValue());
                }
            } else {
                setRemoteSettings(frame);
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
                // If the stream doesn't exist, create it, but we will drop
                // everything because we don't have a listener for it
                // TODO: make sure the lack of listener doesn't cause problems
                if(stream == null) {
                    stream = new Stream();
                    stream.setStreamId(frame.getStreamId());
                    initializeFlowControl(stream.getStreamId());

                    // If we're a server, actually assign the default requestlistener here
                    if(side == SERVER)
                    {
                        // TOOD: get the default request listener?
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
                        throw new GoAwayError(lastClosedRemoteOriginatedStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR,
                                wrapperGen.emptyWrapper());
                }
            } else {
                switch (frame.getFrameType()) {
                    case WINDOW_UPDATE:
                        handleWindowUpdate((Http2WindowUpdate) frame, null);
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
                        } else {
                            throw new GoAwayError(0, Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());
                        }
                    } else {
                        oldData = combined;
                    }
                } else { // Either we got the preface or we don't need it
                    ParserResult parserResult = http2Parser.parse(oldData, newData, decoder);

                    for (Http2Frame frame : parserResult.getParsedFrames()) {
                        log.info("got frame=" + frame);
                        handleFrame(frame);
                    }
                    oldData = parserResult.getMoreData();
                }
            } catch (Http2Error e) {
                channel.write(ByteBuffer.wrap(http2Parser.marshal(e.toFrame()).createByteArray()));
                if(RstStreamError.class.isInstance(e)) {
                    // Mark the stream closed
                    activeStreams.get(((RstStreamError) e).getStreamId()).setStatus(CLOSED);
                }
                if(GoAwayError.class.isInstance(e)) {
                    // TODO: Shut this connection down
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
