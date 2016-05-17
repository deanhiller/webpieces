package org.webpieces.httpproxy.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;

import com.webpieces.httpparser.api.dto.HttpPayload;

public interface FrontendSocket {

	CompletableFuture<FrontendSocket> close();

	CompletableFuture<FrontendSocket> write(HttpPayload payload);

	Channel getUnderlyingChannel();
}
