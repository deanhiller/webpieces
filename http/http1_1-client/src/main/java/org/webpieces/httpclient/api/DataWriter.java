package org.webpieces.httpclient.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpparser.api.dto.HttpData;

public interface DataWriter {

	/**
	 * In the case of ContentLength, this is an HttpData, but in case of Transfer Encoding of chunked, this
	 * is an HttpChunk but both have the same data payload of data
	 */
	public CompletableFuture<DataWriter> incomingData(HttpData data);
	
}
