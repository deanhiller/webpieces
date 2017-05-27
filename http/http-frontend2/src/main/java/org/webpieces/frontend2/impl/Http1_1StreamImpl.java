package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.StreamSession;
import org.webpieces.frontend2.impl.translation.Http2Translations;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class Http1_1StreamImpl implements ResponseStream {
	private static final Logger log = LoggerFactory.getLogger(Http1_1StreamImpl.class);

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private FrontendSocketImpl socket;
	private HttpParser http11Parser;
	private AtomicReference<Http2Msg> endingFrame = new AtomicReference<>();
	private StreamSession session = new StreamSessionImpl();

	private HttpStream streamHandle;

	private int streamId;

	public Http1_1StreamImpl(int streamId, FrontendSocketImpl socket, HttpParser http11Parser) {
		this.streamId = streamId;
		this.socket = socket;
		this.http11Parser = http11Parser;
	}

	@Override
	public CompletableFuture<StreamWriter> sendResponse(Http2Response headers) {
		maybeRemove(headers, headers.isEndOfStream());
		HttpResponse response = Http2Translations.translateResponse(headers);
		Header contentLenHeader = response.getHeaderLookupStruct().getHeader(KnownHeaderName.CONTENT_LENGTH);
		if(contentLenHeader != null) {
			int len = Integer.parseInt(contentLenHeader.getValue());
			if(len != 0) //for redirect firefox content len is 0
				return CompletableFuture.<StreamWriter>completedFuture(new CachingResponseWriter(response, len));
		}
		return write(response).thenApply(c -> new Http11ResponseWriter());
	}

	private class CachingResponseWriter implements StreamWriter {
		private HttpResponse response;
		private int len;
		private DataWrapper allData = dataGen.emptyWrapper();
		
		public CachingResponseWriter(HttpResponse response, int len) {
			this.response = response;
			this.len = len;
		}
		
		@Override
		public CompletableFuture<StreamWriter> processPiece(StreamMsg data) {
			if(!(data instanceof DataFrame))
				throw new UnsupportedOperationException("not supported="+data);
			
			DataFrame frame = (DataFrame) data;
			allData = dataGen.chainDataWrappers(allData, frame.getData());
			if(allData.getReadableSize() > len)
				throw new IllegalArgumentException("Content-Length Header="+len+" but you sent in data totaling="+allData.getReadableSize());
			else if(allData.getReadableSize() < len)
				return CompletableFuture.completedFuture(this);
			
			response.setBody(allData);
			return write(response).thenApply((s) -> this);
		}
	}
	
	private class Http11ResponseWriter implements StreamWriter {

		@Override
		public CompletableFuture<StreamWriter> processPiece(StreamMsg data) {
			maybeRemove(data, data.isEndOfStream());			
			
			List<HttpPayload> responses = Http2Translations.translate(data);
			CompletableFuture<Channel> future = CompletableFuture.completedFuture(null);
			for(HttpPayload p : responses) {
				future = future.thenCompose( (s) -> write(p));
			}
			return future.thenApply((s) -> this);
		}
	}
	
	private void maybeRemove(Http2Msg data, boolean isEnd) {
		if(endingFrame.get() != null)
			throw new IllegalStateException("You had already sent a frame with endOfStream "
					+ "set and can't send more.  ending frame was="+endingFrame+" but you just sent="+data);
		
		Http1_1StreamImpl current = socket.getCurrentStream();
		if(current != this)
			throw new IllegalStateException("Due to http1.1 spec, YOU MUST return "
					+ "responses in order and this is not the current response that needs responding to");

		if(!isEnd)
			return;
		
		endingFrame.set(data);
		socket.removeStream(this);
	}
	
	private CompletableFuture<Channel> write(HttpPayload payload) {
		ByteBuffer buf = http11Parser.marshalToByteBuffer(payload);
		return socket.getChannel().write(buf);
	}
	
	@Override
	public PushStreamHandle openPushStream() {
		throw new UnsupportedOperationException("not supported for http1.1 requests");
	}

	@Override
	public CompletableFuture<Void> cancelStream() {
		throw new UnsupportedOperationException("not supported for http1.1 requests.  you can use getSocket().close() instead if you like");
	}

	@Override
	public FrontendSocket getSocket() {
		return socket;
	}

	@Override
	public StreamSession getSession() {
		return session;
	}

	public void setStreamHandle(HttpStream streamHandle2) {
		this.streamHandle = streamHandle2;
	}

	public HttpStream getStreamHandle() {
		return streamHandle;
	}

	public int getStreamId() {
		return streamId;
	}

}
