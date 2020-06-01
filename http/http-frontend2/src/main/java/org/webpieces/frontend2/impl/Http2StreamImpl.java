package org.webpieces.frontend2.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import com.webpieces.http2parser.api.dto.CancelReason;
import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.ResponseStream;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.ResponseStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;

public class Http2StreamImpl implements ResponseStream {

	private FrontendSocketImpl socket;
	private AtomicBoolean sentResponseHeaders = new AtomicBoolean(false);
	private ResponseStreamHandle responseHandler;
	private Map<String, Object> session = new HashMap<String, Object>();
	private int streamId;

	public Http2StreamImpl(FrontendSocketImpl socket, ResponseStreamHandle responseHandler, int streamId) {
		this.socket = socket;
		this.responseHandler = responseHandler;
		this.streamId = streamId;
	}

	@Override
	public CompletableFuture<StreamWriter> process(Http2Response resp) {
		sentResponseHeaders.set(true);
		return responseHandler.process(resp);
	}

	@Override
	public PushStreamHandle openPushStream() {
		PushStreamHandle pushStream = responseHandler.openPushStream();
		return new Http2PushStreamHandle( pushStream, sentResponseHeaders);
	}

	@Override
	public CompletableFuture<Void> cancel(CancelReason reason) {
		RstStreamFrame frame = new RstStreamFrame(streamId, Http2ErrorCode.CANCEL);
		return responseHandler.cancel(frame);
	}

	@Override
	public FrontendSocket getSocket() {
		return socket;
	}

	@Override
	public Map<String, Object> getSession() {
		return session;
	}

	@Override
	public String toString() {
		return "Http2Stream["+ socket + ", sId=" + streamId + "]";
	}
}
