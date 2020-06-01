package com.webpieces.http2engine.impl.client;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.hpack.api.dto.Http2Trailers;
import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.ResponseStreamHandle;
import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.client.ClientEngineListener;
import com.webpieces.http2engine.api.error.ShutdownConnection;
import com.webpieces.http2engine.impl.shared.EngineResultListener;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class Level8NotifyClntListeners implements EngineResultListener {

	//private static final Logger log = LoggerFactory.getLogger(Level8NotifyClntListeners.class);

	private ClientEngineListener listener;

	public Level8NotifyClntListeners(ClientEngineListener socketListener) {
		this.listener = socketListener;
	}

	public CompletableFuture<Void> sendPreface(DataWrapper prefaceData) {
		ByteBuffer buffer = ByteBuffer.wrap(prefaceData.createByteArray());
		return sendToSocket(buffer);
	}

	@Override
	public CompletableFuture<Void> sendToSocket(ByteBuffer buffer) {
		return listener.sendToSocket(buffer);
	}

	@Override
	public CompletableFuture<Void> sendControlFrameToClient(Http2Msg msg) {
		CompletableFuture<Void> future = new CompletableFuture<Void>();
		try {
			if(msg instanceof GoAwayFrame) {
				listener.sendControlFrameToClient((Http2Frame) msg);
			} else
				throw new UnsupportedOperationException("not done yet. frame="+msg);
			
			future.complete(null);
		} catch(Throwable e) {
			future.completeExceptionally(new RuntimeException(e));
		}
		return future;
	}
	
	public void farEndClosed() {
		this.listener.engineClosedByFarEnd();
	}

	@Override
	public void closeSocket(ShutdownConnection reason) {
		listener.closeSocket(reason);
	}
	
	@Override
	public CompletableFuture<Void> sendRstToApp(Stream stream, CancelReason payload) {
		if(stream instanceof ClientStream) {
			ClientStream str = (ClientStream) stream;
			StreamRef responseStreamRef = str.getResponseStreamRef();
			return responseStreamRef.cancel(payload);
		}
		
		ClientPushStream str = (ClientPushStream) stream;
		PushStreamHandle handle = str.getPushStreamHandle();
		return handle.cancelPush(payload);
	}
	
	@Override
	public CompletableFuture<Void> sendPieceToApp(Stream stream, StreamMsg payload) {
		ClientStream str = (ClientStream) stream;
		CompletableFuture<StreamWriter> writer = str.getResponseStreamRef().getWriter();
		return writer.thenCompose(w -> w.processPiece(payload));
	}

	@Override
	public CompletableFuture<Void> sendPieceToApp(Stream stream, Http2Trailers payload) {
		ClientStream str = (ClientStream) stream;
		CompletableFuture<StreamWriter> writer = str.getResponseStreamRef().getWriter();
		return writer.thenCompose(w -> w.processPiece(payload));
	}

	public CompletableFuture<Void> sendResponseToApp(Stream stream, Http2Response response) {
		if(stream instanceof ClientStream) {
			ClientStream str = (ClientStream) stream;
			ResponseStreamHandle listener = str.getResponseListener();
			
			StreamRef ref = listener.process(response);
			str.setResponseStreamRef(ref);
			
			return ref.getWriter().thenApply(w -> null);
		}
		
		ClientPushStream str = (ClientPushStream) stream;
		PushPromiseListener pushListener = str.getPushPromiseListener();
		return pushListener.processPushResponse(response)
					.thenApply( w -> {
						str.setPushResponseWriter(w);
						return null;
					});
	}

	public CompletableFuture<Void> sendPushToApp(ClientPushStream stream, Http2Push fullPromise) {
		ResponseStreamHandle listener = stream.getOriginalResponseListener();
		PushStreamHandle pushHandle = listener.openPushStream();
		stream.setPushStreamHandle(pushHandle);
		return pushHandle.process(fullPromise)
				.thenApply( l -> {
					stream.setPushPromiseListener(l);
					return null;
				});
	}

}
