package org.webpieces.router.api;

import java.util.Map;

import com.webpieces.http2.api.streaming.ResponseStreamHandle;

public interface RouterResponseHandler extends ResponseStreamHandle {

    /**
     * returns FrontendSocket for webpieces or whatever socket for other implementations wire
     * into this router
     *
     * The socket has a session for storing data common among all requests of the socket
     */
    Object getSocket();
    
    /**
     * Closes socket closing ALL streams that may be in process on this socket so use sparingly unless you 
     * know what you are doing.  Prefer calling cancel to cancel the specific stream
     */
    public void closeSocket(String reason);

    Map<String, Object> getSession();

    boolean requestCameFromHttpsSocket();
    
    boolean requestCameFromBackendSocket();

    /**
     * Use cancel instead which for http2 cancels the single stream or closeSocket(reason) the socket depending on the
     * reason you give it.  For http1.1, cancel shutdowns the stream if the request did not have a keep alive
     * 
     * @return
     * @deprecated
     */
    @Deprecated
    Void closeIfNeeded();
    
}
