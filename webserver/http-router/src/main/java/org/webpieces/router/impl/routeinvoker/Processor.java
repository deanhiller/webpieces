package org.webpieces.router.impl.routeinvoker;

import org.webpieces.util.futures.XFuture;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

public interface Processor {

	XFuture<Void> continueProcessing(MethodMeta meta, Action controllerResponse, ProxyStreamHandle handle);

}
