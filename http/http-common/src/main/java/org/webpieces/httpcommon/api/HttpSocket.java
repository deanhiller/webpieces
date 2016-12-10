package org.webpieces.httpcommon.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;

public interface HttpSocket {
    CompletableFuture<Void> closeSocket();

    Channel getUnderlyingChannel();
}

