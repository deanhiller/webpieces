package org.webpieces.router.impl;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2.api.streaming.StreamWriter;

@Singleton
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
