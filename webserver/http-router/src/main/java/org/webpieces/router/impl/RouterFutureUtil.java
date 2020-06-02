package org.webpieces.router.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.inject.Inject;

import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2engine.api.StreamWriter;

public class RouterFutureUtil {

	private FutureHelper futureUtil;

	@Inject
	public RouterFutureUtil(FutureHelper futureUtil) {
		this.futureUtil = futureUtil;
	}
	
	public CompletableFuture<StreamWriter> failedFuture(Throwable e) {
		return futureUtil.failedFuture(e);
	}

}
