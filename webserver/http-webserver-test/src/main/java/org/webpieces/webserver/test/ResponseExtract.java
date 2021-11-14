package org.webpieces.webserver.test;

import org.webpieces.util.futures.XFuture;
import java.util.concurrent.TimeUnit;

import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.util.exceptions.SneakyThrow;
import org.webpieces.webserver.test.http2.ResponseWrapperHttp2;


public class ResponseExtract {

	public static ResponseWrapper waitResponseAndWrap(XFuture<HttpFullResponse> respFuture) {
		try {
			HttpFullResponse resp = respFuture.get(2, TimeUnit.SECONDS);
			return new ResponseWrapper(resp);
		} catch (Throwable e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	public static ResponseWrapperHttp2 waitAndWrap(XFuture<FullResponse> respFuture) {
		try {
			FullResponse resp = respFuture.get(2, TimeUnit.SECONDS);
			return new ResponseWrapperHttp2(resp);
		} catch (Throwable e) {
			throw SneakyThrow.sneak(e);
		}
	}	

}
