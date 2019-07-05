package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.controller.actions.Action;

public interface Processor {

	CompletableFuture<Void> continueProcessing(Action controllerResponse, ResponseStreamer responseCb);

}
