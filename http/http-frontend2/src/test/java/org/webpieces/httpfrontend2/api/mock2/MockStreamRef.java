package org.webpieces.httpfrontend2.api.mock2;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.Cancel;

import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class MockStreamRef implements StreamRef {

	private boolean isCancelled;
	private CompletableFuture<StreamWriter> writer;
	private CancelReason reason;
	
	public MockStreamRef(CompletableFuture<StreamWriter> writer) {
		this.writer = writer;
	}

	public MockStreamRef(MockStreamWriter mockSw) {
		this.writer = CompletableFuture.completedFuture(mockSw);
	}

	@Override
	public CompletableFuture<StreamWriter> getWriter() {
		return writer;
	}

	@Override
	public CompletableFuture<Void> cancel(CancelReason reason) {
		this.reason = reason;
		isCancelled = true;
		return CompletableFuture.completedFuture(null);
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	public CancelReason getCancelInfo() {
		return reason;
	}

}
