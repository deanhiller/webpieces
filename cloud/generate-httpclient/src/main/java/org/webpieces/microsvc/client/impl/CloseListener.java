package org.webpieces.microsvc.client.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketListener;
import org.webpieces.router.api.RouterResponseHandler;

public class CloseListener implements Http2SocketListener {

    private static final Logger log = LoggerFactory.getLogger(CloseListener.class);

    private RouterResponseHandler handle;

    public CloseListener(RouterResponseHandler handle) {
        this.handle = handle;
    }

    @Override
    public void socketFarEndClosed(Http2Socket socket) {

        log.error("Client closed it's socket so we are closing client's socket(FrontendSocket)");

        handle.closeSocket("Client closed it's socket");

    }

}
