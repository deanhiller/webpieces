package org.webpieces.httpcommon.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import com.webpieces.http2parser.api.dto.Http2Settings;

public interface Http2ServerEngine extends Http2Engine {
    // TODO: Figure out if the sendResponse/sendData/sendTrailer should just be in ResponseSender and Http2ServerEngineImpl
    // should just implement ResponseSender as well as Http2ServerEngine.

    /**
     * Sends a response for a given request.
     *
     * @param response The actual response.
     * @param request The request, or implied request, in the case of push promise responses.
     * @param requestId The request id. If sendResponse is called twice or more with the same requestId, then
     *                  the second and subsequent responses are sent as push promise responses.
     * @param isComplete If the request is complete, otherwise data will follow.
     * @return A future with the ResponseId, which we need to use for following data.
     *
     */
    CompletableFuture<ResponseId> sendResponse(HttpResponse response, HttpRequest request, RequestId requestId, boolean isComplete);

    /**
     * Send the body of a request.
     *
     * @param data The piece of the body we're sending.
     * @param id The ResponseId that we got back from sendResponse
     * @param isComplete if this data completes the response.
     * @return
     */
    CompletableFuture<Void> sendData(DataWrapper data, ResponseId id, boolean isComplete);

    /**
     * Gets the ResponseSender which the RequestListener should use to pass back responses to requests.
     *
     * @return
     */
    ResponseSender getResponseSender();


    /**
     * Sets the listener that handles requests as they come in.
     *
     * @param requestListener
     */
    void setRequestListener(RequestListener requestListener);

    /**
     * Given a settings frame, set our notion of the remote side's settings to that.
     *
     * @param frame The settings frame from which to glean the settings
     * @param sendAck If true, then send an 'ack' frame to the remote side on setting this.
     *                We do 'false' here because if settings are set via HTTP2-Settings, then
     *                we don't want to send an ack. The response to the Upgrade request is
     *                the implied ack.
     */
    void setRemoteSettings(Http2Settings frame, boolean sendAck);
}
