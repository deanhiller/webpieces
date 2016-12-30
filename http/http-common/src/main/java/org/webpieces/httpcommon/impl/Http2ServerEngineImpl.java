package org.webpieces.httpcommon.impl;

import static com.webpieces.http2parser.api.dto.SettingsParameter.SETTINGS_ENABLE_PUSH;
import static com.webpieces.http2parser.api.dto.SettingsParameter.SETTINGS_MAX_CONCURRENT_STREAMS;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.Http2ServerEngine;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2SettingsMap;
import com.webpieces.http2parser.api.dto.Http2Data;
import com.webpieces.http2parser.api.dto.Http2HeadersFrame;
import com.webpieces.http2parser.api.dto.Http2RstStream;
import com.webpieces.http2parser.api.dto.Http2Settings;

public class Http2ServerEngineImpl extends Http2EngineImpl implements Http2ServerEngine {
    private static final Logger log = LoggerFactory.getLogger(Http2ServerEngineImpl.class);

    public Http2ServerEngineImpl(
        Http2Parser http2Parser,
        Channel channel,
        InetSocketAddress remoteAddress,
        Http2SettingsMap http2SettingsMap) {
        super(http2Parser, channel, remoteAddress, http2SettingsMap, HttpSide.SERVER);
    }

    private RequestListener requestListener;
    private ResponseSender responseSender = new Http2ResponseSender(this);

    @Override
    public ResponseSender getResponseSender() {
        return responseSender;
    }

    @Override
    public void setRequestListener(RequestListener requestListener) {
        this.requestListener = requestListener;
    }

    @Override
    public CompletableFuture<ResponseId> sendResponse(HttpResponse response, HttpRequest request, RequestId requestId, boolean isComplete) {
        Stream responseStream = activeStreams.get(requestId.getValue());

        if(responseStream == null) {
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
            log.info("creating a pushed response stream");
            if(remoteSettings.get(SETTINGS_ENABLE_PUSH) == 0) {
                // Enable push is not permitted, so ignore this response and return a -1 response id
                log.info("push promise not permitted by client, ignoring pushed response");
                return CompletableFuture.completedFuture(new ResponseId(0));
            }
            long openStreams = countOpenLocalOriginatedStreams();
            log.info("{} streams are open originated locally", openStreams);
            if(remoteSettings.get(SETTINGS_MAX_CONCURRENT_STREAMS) != null &&
                     openStreams >= remoteSettings.get(SETTINGS_MAX_CONCURRENT_STREAMS)) {
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
                .thenAccept(v -> {
                    // Don't send an empty dataframe that is not completing.
                    if (response.getBodyNonNull().getReadableSize() != 0 || isComplete)
                        sendDataFrames(response.getBodyNonNull(), isComplete, stream, false);
                })
                .thenApply(v -> stream.getResponseId()).exceptionally(e -> {
                    log.error("can't send header frames", e);
                    return stream.getResponseId();
                });
    }

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
        return sendDataFrames(data, isComplete, responseStream, false);
    }

    @Override
    void sideSpecificHandleData(Http2Data frame, int payloadLength, Stream stream) {
        requestListener.incomingData(frame.getData(), stream.getRequestId(), frame.isEndStream(), responseSender);
    }

    @Override
    void sideSpecificHandleHeaders(Http2HeadersFrame frame, boolean isTrailer, Stream stream) {
        if(isTrailer) {
            requestListener.incomingTrailer(frame.getHeaderList(), stream.getRequestId(), frame.isEndStream(), responseSender);
        } else {
            HttpRequest request = requestFromHeaders(frame.getHeaderList(), stream);
            checkHeaders(request.getHeaderLookupStruct(), stream);
            stream.setRequest(request);
            requestListener.incomingRequest(request, stream.getRequestId(), frame.isEndStream(), responseSender);
        }
    }

    @Override
    void sideSpecificHandleRstStream(Http2RstStream frame, Stream stream) {
        // TODO: change incomingError to failure and fix the exception types
        responseSender.sendException(null);
    }

    @Override
    public void setRemoteSettings(Http2Settings frame, boolean sendAck) {
        super.setRemoteSettings(frame, sendAck);
    }
}
