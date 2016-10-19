package org.webpieces.httpcommon.api;

import org.webpieces.nio.api.channels.Channel;

import java.util.concurrent.CompletableFuture;

public interface HttpSocket {
    CompletableFuture<Void> closeSocket();

    Channel getUnderlyingChannel();
}

