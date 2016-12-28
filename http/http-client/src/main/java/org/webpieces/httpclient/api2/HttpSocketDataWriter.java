package org.webpieces.httpclient.api2;

import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpLastChunk;

public interface HttpSocketDataWriter {

	/**
	 * Send HttpLastChunk to end the streaming of the chunks
	 * @param resp
	 */
	void sendData(HttpChunk resp);

	void sendLastData(HttpLastChunk lastChunk);

	/**
	 * In http/2, sends a stream reset to cancel the request.  In http1.1, throws an exception
	 * since requests can't be cancelled
	 */
	void cancel();
	
}
