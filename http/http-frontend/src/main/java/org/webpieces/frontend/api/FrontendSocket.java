package org.webpieces.frontend.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.nio.api.channels.Channel;

public interface FrontendSocket {

	CompletableFuture<FrontendSocket> close();

	CompletableFuture<FrontendSocket> write(HttpPayload payload);

	Channel getUnderlyingChannel();
}
