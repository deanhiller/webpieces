package org.webpieces.router.api;

import org.webpieces.util.futures.XFuture;

import org.webpieces.router.api.controller.actions.HttpPort;
import org.webpieces.router.api.routes.RouteId;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;

public interface RouterStreamHandle extends RouterResponseHandler {


    /**
     * Creates a base response off the request looking for things like keep-alive so the response
     * conforms to http specification.
     * 
     * This does NOT send a response so you still need to call handler.process(response) to send it back to client
     * 
     * @param statusReason TODO
     */
    Http2Response createBaseResponse(Http2Request req, String mimeType, int statusCode, String statusReason);

	XFuture<Void> sendFullRedirect(RouteId id, Object ... args);

	XFuture<Void> sendAjaxRedirect(RouteId id, Object ... args);

	XFuture<Void> sendPortRedirect(HttpPort port, RouteId id, Object ... args);

}
