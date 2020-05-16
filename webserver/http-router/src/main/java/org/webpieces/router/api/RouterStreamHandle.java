package org.webpieces.router.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.controller.actions.HttpPort;
import org.webpieces.router.api.routes.RouteId;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;

public interface RouterStreamHandle extends RouterResponseHandler {


    /**
     * Creates a base response off the request looking for things like keep-alive so the response
     * conforms to http specification 
     * @param statusReason TODO
     */
    Http2Response createBaseResponse(Http2Request req, String mimeType, int statusCode, String statusReason);

	CompletableFuture<Void> createFullRedirect(RouteId id, Object ... args);

	CompletableFuture<Void> createAjaxRedirect(RouteId id, Object ... args);

	CompletableFuture<Void> createPortRedirect(HttpPort port, RouteId id, Object ... args);

}
