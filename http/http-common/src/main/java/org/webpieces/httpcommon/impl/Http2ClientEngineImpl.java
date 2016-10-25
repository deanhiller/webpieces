package org.webpieces.httpcommon.impl;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.dto.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.Http2Data;
import com.webpieces.http2parser.api.dto.Http2Headers;
import com.webpieces.http2parser.api.dto.Http2RstStream;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.Http2ClientEngine;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseListener;
import org.webpieces.httpcommon.api.exceptions.ClientError;
import org.webpieces.httpcommon.api.exceptions.RstStreamError;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

import static com.webpieces.http2parser.api.dto.Http2Settings.Parameter.SETTINGS_MAX_CONCURRENT_STREAMS;
import static org.webpieces.httpcommon.api.Http2Engine.HttpSide.CLIENT;

public class Http2ClientEngineImpl extends Http2EngineImpl implements Http2ClientEngine {
    private static final Logger log = LoggerFactory.getLogger(Http2ServerEngineImpl.class);

    public Http2ClientEngineImpl(Http2Parser http2Parser, Channel channel, InetSocketAddress remoteAddress) {
        super(http2Parser, channel, remoteAddress, HttpSide.CLIENT);
    }

    @Override
    void sideSpecificHandleData(Http2Data frame, int payloadLength, Stream stream) {
        stream.getResponseListener().incomingData(frame.getData(), stream.getResponseId(), frame.isEndStream()).thenAccept(
                length -> incrementIncomingWindow(frame.getStreamId(), payloadLength));
    }

    @Override
    void sideSpecificHandleHeaders(Http2Headers frame, boolean isTrailer, Stream stream) {

        if(isTrailer) {
            stream.getResponseListener().incomingTrailer(frame.getHeaderList(), stream.getResponseId(), frame.isEndStream());
        } else {
            HttpResponse response = responseFromHeaders(frame.getHeaderList(), stream);
            checkHeaders(response.getHeaderLookupStruct(), stream);
            stream.setResponse(response);
            stream.getResponseListener().incomingResponse(response, stream.getRequest(), stream.getResponseId(), frame.isEndStream());
        }
    }

    @Override
    void sideSpecificHandleRstStream(Http2RstStream frame, Stream stream) {
        stream.getResponseListener().failure(new RstStreamError(frame.getErrorCode(), stream.getStreamId()));
    }

    @Override
    public void sendHttp2Preface() {
        log.info("sending preface");
        getUnderlyingChannel().write(ByteBuffer.wrap(DatatypeConverter.parseHexBinary(prefaceHexString)));
    }

    @Override
    public RequestId createInitialStream(HttpResponse r, HttpRequest req, ResponseListener listener, DataWrapper leftOverData) {
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
            dataListener.incomingData(getUnderlyingChannel(), ByteBuffer.wrap(responseBody.createByteArray()));

        if(leftOverData.getReadableSize() > 0)
            dataListener.incomingData(getUnderlyingChannel(), ByteBuffer.wrap(leftOverData.createByteArray()));

        return new RequestId(initialStreamId);
    }

    @Override
    public CompletableFuture<Void> sendData(RequestId id, DataWrapper data, boolean isComplete) {
        Stream stream = activeStreams.get(id.getValue());
        return sendDataFrames(data, isComplete, stream, false);
    }

    @Override
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
                        channel -> sendDataFrames(request.getBodyNonNull(), isComplete, newStream, false))
                .thenApply(channel -> new RequestId(thisStreamId));

    }
}
