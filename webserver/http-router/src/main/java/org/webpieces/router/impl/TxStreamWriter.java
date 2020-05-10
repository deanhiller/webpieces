package org.webpieces.router.impl;

import java.util.concurrent.CompletableFuture;

import org.slf4j.MDC;

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class TxStreamWriter implements StreamWriter {

	private String txId;
	private StreamWriter w;

	public TxStreamWriter(String txId, StreamWriter w) {
		this.txId = txId;
		this.w = w;
	}

	@Override
	public CompletableFuture<Void> processPiece(StreamMsg data) {
		MDC.put("txId", txId);
		return w.processPiece(data);
	}

}
