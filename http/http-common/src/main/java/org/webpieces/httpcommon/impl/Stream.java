package org.webpieces.httpcommon.impl;

import static org.webpieces.httpcommon.impl.Stream.StreamStatus.IDLE;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseListener;
import org.webpieces.httpcommon.api.exceptions.RstStreamError;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.PriorityDetails;

class Stream {
    private static final Logger log = LoggerFactory.getLogger(Stream.class);

    enum StreamStatus {
        IDLE,
        RESERVED_LOCAL,
        RESERVED_REMOTE,
        OPEN,
        HALF_CLOSED_LOCAL,
        HALF_CLOSED_REMOTE,
        CLOSED
    }



    private HttpRequest request;
    private HttpResponse response;

    // Only used for client
    private ResponseListener responseListener;

    private int streamId;
    private boolean hasContentLengthHeader = false;
    private long contentLengthHeaderValue;
    private long currentDataLength = 0;

    private List<String> trailerHeaders = new ArrayList<>();
    private boolean trailerEnabled;

    int getStreamId() {
        return streamId;
    }

    RequestId getRequestId() { return new RequestId(streamId); }
    ResponseId getResponseId() { return new ResponseId(streamId); }

    void setStreamId(int streamId) {
        this.streamId = streamId;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    void setResponseListener(ResponseListener responseListener) {
        this.responseListener = responseListener;
    }

    private StreamStatus status = IDLE;
    private PriorityDetails priorityDetails;

    public PriorityDetails getPriorityDetails() {
        return priorityDetails;
    }

    void setPriorityDetails(PriorityDetails priorityDetails) {
        this.priorityDetails = priorityDetails;
    }

    public boolean isStreamDependencyIsExclusive() {
        return priorityDetails.isStreamDependencyIsExclusive();
    }

    public int getStreamDependency() {
        return priorityDetails.getStreamDependency();
    }

    public short getWeight() {
        return priorityDetails.getWeight();
    }

    StreamStatus getStatus() {
        return status;
    }

    void setStatus(StreamStatus status) {
        log.info("{}: {} -> {}", streamId, this.status, status);
        this.status = status;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public HttpResponse getResponse() {
        return response;
    }

    ResponseListener getResponseListener() {
        return responseListener;
    }

    void setContentLengthHeaderValue(long contentLengthHeaderValue) {
        this.hasContentLengthHeader = true;
        this.contentLengthHeaderValue = contentLengthHeaderValue;
    }

    /* Return false if we have a problem and should throw */
    void checkAgainstContentLength(int dataLength, boolean isComplete) {
        currentDataLength += dataLength;
        if ((isComplete && currentDataLength != contentLengthHeaderValue) || (currentDataLength > contentLengthHeaderValue))
            throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, streamId);
    }

    void addTrailerHeader(String header) {
        trailerEnabled = true;
        trailerHeaders.add(header);
    }

    boolean isTrailerEnabled() {
        return trailerEnabled;
    }

    public List<String> getTrailerHeaders() {
        return trailerHeaders;
    }
}
