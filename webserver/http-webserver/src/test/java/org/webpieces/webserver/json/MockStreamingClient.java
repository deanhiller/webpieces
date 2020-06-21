package org.webpieces.webserver.json;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.webserver.json.app.EchoStreamingClient;

import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class MockStreamingClient extends EchoStreamingClient {

	private List<CompletableFuture<StreamWriter>> writers = new ArrayList<CompletableFuture<StreamWriter>>();

	@Override
	public StreamRef stream(ResponseStreamHandle handle) {
		CompletableFuture<StreamWriter> writer = writers.remove(0);
		return new RouterStreamRef("mock", writer, null);
	}

	public void addStreamWriter(CompletableFuture<StreamWriter> writer) {
		writers .add(writer);
	}
}
