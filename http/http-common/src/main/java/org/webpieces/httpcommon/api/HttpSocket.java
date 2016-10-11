package org.webpieces.httpcommon.api;

import java.util.concurrent.CompletableFuture;

public interface HttpSocket {
    CompletableFuture<Void> closeSocket();
}

