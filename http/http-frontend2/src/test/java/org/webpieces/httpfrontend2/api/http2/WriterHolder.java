package org.webpieces.httpfrontend2.api.http2;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;

public class WriterHolder {

	private StreamWriter writer1;
	private CompletableFuture<StreamWriter> future2;
	private Http2Headers resp1;
	private Http2Headers resp2;

	public WriterHolder(StreamWriter writer1, CompletableFuture<StreamWriter> future2, Http2Headers resp1, Http2Headers resp2) {
		this.writer1 = writer1;
		this.future2 = future2;
		this.resp1 = resp1;
		this.resp2 = resp2;
	}

	public StreamWriter getWriter1() {
		return writer1;
	}

	public CompletableFuture<StreamWriter> getFuture2() {
		return future2;
	}

	public Http2Headers getResp1() {
		return resp1;
	}

	public Http2Headers getResp2() {
		return resp2;
	}

	
}
