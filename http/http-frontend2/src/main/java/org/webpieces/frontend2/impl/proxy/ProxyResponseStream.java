package org.webpieces.frontend2.impl.proxy;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.frontend2.api.StreamSession;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;

public class ProxyResponseStream implements ResponseStream {

	private ResponseStream stream;
	private Queue<Object> lastFewFrames = new LinkedList<>();

	public ProxyResponseStream(ResponseStream stream) {
		this.stream = stream;
	}
	
	@Override
	public CompletableFuture<StreamWriter> sendResponse(Http2Response headers) {
		lastFewFrames.add(headers);
		return stream.sendResponse(headers).thenApply((w) -> {
			return new ProxyStreamWriter(w, lastFewFrames);
		});
	}

	@Override
	public PushStreamHandle openPushStream() {
		return new ProxyPushStreamHandle(stream.openPushStream());
	}

	@Override
	public CompletableFuture<Void> cancelStream() {
		return stream.cancelStream();
	}

	@Override
	public FrontendSocket getSocket() {
		return stream.getSocket();
	}

	@Override
	public StreamSession getSession() {
		return stream.getSession();
	}

	@Override
	public String toString() {
		return getSocket()+"";
	}

}
