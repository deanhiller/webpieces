package org.webpieces.httpclient.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.http2translations.api.Http2ToHttp1_1;
import org.webpieces.httpclient.api.HttpDataWriter;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpRequest;

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class StreamWriterImpl implements StreamWriter {

	private HttpDataWriter dataWriter;
	private HttpRequest req;

	public StreamWriterImpl(HttpDataWriter dataWriter, HttpRequest req) {
		this.dataWriter = dataWriter;
		this.req = req;
	}

	@Override
	public CompletableFuture<Void> processPiece(StreamMsg data) {
		if(!(data instanceof DataFrame))
			throw new IllegalArgumentException("You must feed in http1_1 compatible http2 payloads like DataFrame.  this is not http1_1 compatible="+data.getClass());
		HttpData chunk = Http2ToHttp1_1.translate((DataFrame)data, req);
		return dataWriter.send(chunk);
	}

}
