package org.webpieces.router.impl;

import org.webpieces.util.futures.XFuture;

import org.slf4j.MDC;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;

public class TxStreamWriter implements StreamWriter {

	private String txId;
	private StreamWriter w;

	public TxStreamWriter(String txId, StreamWriter w) {
		this.txId = txId;
		this.w = w;
	}

	@Override
	public XFuture<Void> processPiece(StreamMsg data) {
		MDC.put("txId", txId);
		try {
			return w.processPiece(data);
		} finally {
			MDC.remove(txId);
		}
	}

}
