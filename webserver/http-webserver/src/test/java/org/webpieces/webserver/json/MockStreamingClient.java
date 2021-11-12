package org.webpieces.webserver.json;

import java.util.ArrayList;
import java.util.List;
import org.webpieces.util.futures.XFuture;

import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.webserver.json.app.EchoStreamingClient;

import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class MockStreamingClient extends EchoStreamingClient {

	private List<XFuture<StreamWriter>> writers = new ArrayList<XFuture<StreamWriter>>();

	@Override
	public StreamRef stream(ResponseStreamHandle handle) {
		XFuture<StreamWriter> writer = writers.remove(0);
		return new RouterStreamRef("mock", writer, null);
	}

	public void addStreamWriter(XFuture<StreamWriter> writer) {
		writers .add(writer);
	}
}
