package org.webpieces.httpclient.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient.api.HttpDataWriter;
import org.webpieces.httpparser.api.dto.HttpData;

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class StreamWriterImpl implements StreamWriter {

	private HttpDataWriter dataWriter;

	public StreamWriterImpl(HttpDataWriter dataWriter) {
		this.dataWriter = dataWriter;
	}

	@Override
	public CompletableFuture<StreamWriter> processPiece(StreamMsg data) {
		HttpData chunk = Translations.translate(data);
		return dataWriter.send(chunk).thenApply(s -> this);
	}

}
