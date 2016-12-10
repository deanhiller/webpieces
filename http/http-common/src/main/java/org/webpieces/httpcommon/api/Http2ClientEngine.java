package org.webpieces.httpcommon.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import com.webpieces.http2parser.api.dto.Http2Settings;

public interface Http2ClientEngine extends Http2Engine {
    // These are both needed to give the RequestSender the ability to deal with negotiating http1.1 vs http2.
    RequestId createInitialStream(HttpResponse r, HttpRequest req, ResponseListener listener, DataWrapper leftOverData);

    /**
     * Sends the HTTP2 preface to the server.
     *
     */
    void sendHttp2Preface();


    /**
     * Clean up pending requests/active streams, send messages appropriately.
     *
     * TODO: Should this be both client & server?
     *
     * @param msg the msg to send to the various listeners that the request was canceled before completion.
     *
     */
    void cleanUpPendings(String msg);


    /**
     * Gets the local settings that we want to request. This is used to create the HTTP2-Settings
     * header when HTTP/1.1 is negotiating to upgrade to HTTP/2.
     *
     * @return
     */
    Http2Settings getLocalRequestedSettingsFrame();


    /**
     * Sends a request via HTTP2 to the server connected on the remote side.
     *
     * @param request The basic request.
     * @param isComplete If this request contains the whole body and completes the request.
     * @param listener When the request returns a response, who is listening for that.
     * @return A future of the RequestId, which we need to send subsequent data on this request.
     *
     */
    CompletableFuture<RequestId> sendRequest(HttpRequest request, boolean isComplete, ResponseListener listener);


    /**
     * Sends data associated with a particular request.
     *
     * @param id The RequestId that sendRequest gave us.
     * @param data The data
     * @param isComplete If this completes the request.
     * @return Once the data is written out, then the future completes.
     *
     */
    CompletableFuture<Void> sendData(RequestId id, DataWrapper data, boolean isComplete);
}
