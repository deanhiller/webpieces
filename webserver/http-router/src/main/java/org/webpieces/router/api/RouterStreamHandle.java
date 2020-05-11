package org.webpieces.router.api;

import java.util.Map;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.ResponseHandler;

public interface RouterStreamHandle extends ResponseHandler {

    /**
     * returns FrontendSocket for webpieces or whatever socket for other implementations wire
     * into this router
     *
     * The socket has a session for storing data common among all requests of the socket
     */
    Object getSocket();

    Map<String, Object> getSession();

    boolean requestCameFromHttpsSocket();
    
    boolean requestCameFromBackendSocket();

    /**
     * Temporary for refactoring only
     * @return
     * @deprecated
     */
    @Deprecated
    Void closeIfNeeded();

    /**
     * Creates a base response off the request looking for things like keep-alive so the response
     * conforms to http specification 
     * @param statusReason TODO
     */
    Http2Response createBaseResponse(Http2Request req, String mimeType, int statusCode, String statusReason);

}
