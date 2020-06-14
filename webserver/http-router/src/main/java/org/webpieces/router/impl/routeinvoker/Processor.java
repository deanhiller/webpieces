package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

public interface Processor {

	CompletableFuture<Void> continueProcessing(MethodMeta meta, Action controllerResponse, ProxyStreamHandle handle);

}
