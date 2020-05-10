package org.webpieces.router.impl.proxyout;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.OverwritePlatformResponse;
import org.webpieces.router.api.RouterStreamHandle;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;

public class ResponseOverrideSender {

	private RouterStreamHandle stream;

	public ResponseOverrideSender(RouterStreamHandle stream) {
		this.stream = stream;
	}

	@Override
	public String toString() {
		return "ResponseOverrideSender [responseSender=" + stream + "]";
	}

	public CompletableFuture<StreamWriter> sendResponse(Http2Response response) {		
		return stream.process(response);
	}

	@Deprecated
	public void close() {
		stream.closeIfNeeded();
	}

}
