package org.webpieces.httpclient11.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpclient11.api.HttpSocketListener;

public class ProxyClose implements HttpSocketListener {
    private static final Logger log = LoggerFactory.getLogger(ProxyClose.class);
    private HttpSocketListener socketListener;
    private String svrSocket;

    public ProxyClose(HttpSocketListener socketListener, String svrSocket) {
        this.socketListener = socketListener;
        this.svrSocket = svrSocket;
    }

    @Override
    public void socketClosed(HttpSocket socket) {
        try {
            MDC.put("svrSocket", svrSocket);
            log.info("far end closed. socket("+socket+")");
            socketListener.socketClosed(socket);
        } finally {
            MDC.remove("svrSocket");
        }
    }
}
