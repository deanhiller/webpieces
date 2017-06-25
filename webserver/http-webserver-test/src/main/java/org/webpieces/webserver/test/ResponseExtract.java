package org.webpieces.webserver.test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.webpieces.httpclient11.api.HttpFullResponse;


public class ResponseExtract {

	public static FullResponse waitResponseAndWrap(CompletableFuture<HttpFullResponse> respFuture) {
		try {
			HttpFullResponse resp = respFuture.get(2, TimeUnit.SECONDS);
			return new FullResponse(resp);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

}
