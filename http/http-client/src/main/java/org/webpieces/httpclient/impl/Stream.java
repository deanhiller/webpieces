package org.webpieces.httpclient.impl;

import org.webpieces.httpclient.api.ResponseListener;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;


import java.util.List;
import java.util.Map;

import static org.webpieces.httpclient.impl.Stream.StreamStatus.IDLE;

public class Stream {

    public enum StreamStatus {
        IDLE,
        RESERVED_LOCAL,
        RESERVED_REMOTE,
        OPEN,
        HALF_CLOSED_LOCAL,
        HALF_CLOSED_REMOTE,
        CLOSED,

        // I made up these two states to handle continuation
        WAITING_MORE_NORMAL_HEADERS,
        WAITING_MORE_PUSH_PROMISE_HEADERS
    }



    private HttpRequest request;
    private HttpResponse response;
    private ResponseListener listener;
    private Map<String, String> pushPromiseHeaders;
    private Map<String, String> headerHeaders;

    public Map<String, String> getPushPromiseHeaders() {
        return pushPromiseHeaders;
    }

    public void setPushPromiseHeaders(Map<String, String> pushPromiseHeaders) {
        this.pushPromiseHeaders = pushPromiseHeaders;
    }

    public Map<String, String> getHeaderHeaders() {
        return headerHeaders;
    }

    public void setHeaderHeaders(Map<String, String> headerHeaders) {
        this.headerHeaders = headerHeaders;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    public void setListener(ResponseListener listener) {
        this.listener = listener;
    }

    private StreamStatus status = IDLE;
    private int windowIncrement = 0;
    private int priority = 0;

    public int getWindowIncrement() {
        return windowIncrement;
    }

    public void setWindowIncrement(int windowIncrement) {
        this.windowIncrement = windowIncrement;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public StreamStatus getStatus() {
        return status;
    }

    public void setStatus(StreamStatus status) {
        this.status = status;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public ResponseListener getListener() {
        return listener;
    }
}
