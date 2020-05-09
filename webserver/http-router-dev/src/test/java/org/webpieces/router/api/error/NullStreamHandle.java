package org.webpieces.router.api.error;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.RouterStreamHandle;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class NullStreamHandle implements RouterStreamHandle {

	@Override
	public CompletableFuture<StreamWriter> process(Http2Response response) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PushStreamHandle openPushStream() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompletableFuture<Void> cancel(CancelReason payload) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getSocket() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Object> getSession() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean requestCameFromHttpsSocket() {
		return false;
	}

	@Override
	public boolean requestCameFromBackendSocket() {
		return false;
	}

	@Override
	public Void closeIfNeeded() {
		throw new UnsupportedOperationException();
	}

}
