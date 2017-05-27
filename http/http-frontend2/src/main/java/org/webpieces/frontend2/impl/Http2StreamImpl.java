package org.webpieces.frontend2.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.api.StreamSession;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.ResponseHandler2;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;

public class Http2StreamImpl implements FrontendStream {

	private FrontendSocketImpl socket;
	private AtomicBoolean sentResponseHeaders = new AtomicBoolean(false);
	private ResponseHandler2 responseHandler;
	private StreamSession session = new StreamSessionImpl();
	private int streamId;

	public Http2StreamImpl(FrontendSocketImpl socket, ResponseHandler2 responseHandler, int streamId) {
		this.socket = socket;
		this.responseHandler = responseHandler;
		this.streamId = streamId;
	}

	@Override
	public CompletableFuture<StreamWriter> sendResponse(Http2Response resp) {
		sentResponseHeaders.set(true);
		return responseHandler.process(resp);
	}

	@Override
	public PushStreamHandle openPushStream() {
		PushStreamHandle pushStream = responseHandler.openPushStream();
		return new Http2PushStreamHandle( pushStream, sentResponseHeaders);
	}

	@Override
	public CompletableFuture<Void> cancelStream() {
		RstStreamFrame frame = new RstStreamFrame(streamId, Http2ErrorCode.CANCEL);
		return responseHandler.cancel(frame);
	}

	@Override
	public FrontendSocket getSocket() {
		return socket;
	}

	@Override
	public StreamSession getSession() {
		return session;
	}

	@Override
	public String toString() {
		return "Http2Stream["+ socket + ", sId=" + streamId + "]";
	}
}
