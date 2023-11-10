package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.webpieces.util.exceptions.NioClosedChannelException;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.http2translations.api.Http2ToHttp11;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpLastChunk;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.util.locking.PermitQueue;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.Http2Method;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.PushStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class Http11StreamImpl implements ResponseStream {
	private static final Logger log = LoggerFactory.getLogger(Http11StreamImpl.class);
	//private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private FrontendSocketImpl socket;
	private HttpParser http11Parser;
	private AtomicReference<Http2Msg> endingFrame = new AtomicReference<>();
	private Map<String, Object> session = new HashMap<String, Object>();

	private HttpStream streamHandle;

	private int streamId;

	private PermitQueue permitQueue;

	private boolean sentFullRequest;

	private Http2Request http2Request;

	private HttpRequest http1Req;

	private boolean isForConnectRequeest;
	private boolean hasRespondedToConnect;
	private StreamRef streamRef;

	public Http11StreamImpl(
			int streamId, 
			FrontendSocketImpl socket, 
			HttpParser http11Parser, 
			PermitQueue permitQueue, 
			HttpRequest http1Req, 
			Http2Request headers
	) {
		this.streamId = streamId;
		this.socket = socket;
		this.http11Parser = http11Parser;
		this.permitQueue = permitQueue;
		this.http1Req = http1Req;
		this.http2Request = headers;
		if(headers.getKnownMethod() == Http2Method.CONNECT)
			isForConnectRequeest = true;
	}

	@Override
	public XFuture<StreamWriter> process(Http2Response headers) {
		closeCheck(headers);
		HttpResponse response = Http2ToHttp11.translateResponse(headers);
		
		if(http2Request.getKnownMethod() == Http2Method.CONNECT) {
			//In this case, it is an upgrade to a bi-directional stream
			//connect has no content length BUT we are basically creating a 'stream' here of
			//bytes so we don't care about parsing anymore(ie. SSL or http)..
			return write(response).thenApply(c -> new Http11ChunkedWriter(http1Req, http2Request));
		} else if(headers.isEndOfStream()) {
			validateHeader(response);
			remove(headers);
			return write(response).thenApply(w -> {
				permitQueue.releasePermit();
				return new NoWritesWriter();
			});
		} else if(contentLengthGreaterThanZero(headers)) {
			return write(response).thenApply(w -> new ContentLengthResponseWriter(headers));
		}
		
		return write(response).thenApply(c -> new Http11ChunkedWriter(http1Req, http2Request));
	}

	private void closeCheck(Http2Msg msg) {
		if(endingFrame.get() != null)
			throw new IllegalArgumentException("You already sent a frame with endOfStream=true so cannot send more data."
					+ "  You already sent\n"+endingFrame.get()+"\n\nAnd NOW, you are trying to send=\n"+msg);
	}

	private void validateHeader(HttpResponse response) {
		Header contentLenHeader = response.getHeaderLookupStruct().getHeader(KnownHeaderName.CONTENT_LENGTH);
		if(contentLenHeader == null)
			throw new IllegalArgumentException("Content Length header required and missing and should be set to zero");
		else if(contentLenHeader.getValue() == null)
			throw new IllegalArgumentException("Content Length header found but it's value is null");
		
		int len = Integer.parseInt(contentLenHeader.getValue());
		if(len != 0)
			throw new IllegalArgumentException("Content Length header found but it's value is 0 while response.isEndOfStream is true.  this is contradictory");
	}

	private boolean contentLengthGreaterThanZero(Http2Response headers) {
		Http2Header contentLenHeader = headers.getHeaderLookupStruct().getHeader(Http2HeaderName.CONTENT_LENGTH);
		if(contentLenHeader != null) {
			int len = Integer.parseInt(contentLenHeader.getValue());
			if(len > 0) //for redirect firefox content len is 0
				return true;
			else if(len == 0) {
				if(!headers.isEndOfStream())
					throw new IllegalStateException("Content-Length=0 but response.isEndOfStream==false");
			}
		}
		return false;
	}

	private class NoWritesWriter implements StreamWriter {
		@Override
		public XFuture<Void> processPiece(StreamMsg data) {
			XFuture<Void> future = new XFuture<>();
			future.completeExceptionally(new IllegalStateException("You already sent a response with endStream==true"));
			return future;
		}

	}
	
	private class ContentLengthResponseWriter implements StreamWriter {
		private int len;
		private int totalWritten;
		
		public ContentLengthResponseWriter(Http2Response response) {
			Http2Header contentLenHeader = response.getHeaderLookupStruct().getHeader(Http2HeaderName.CONTENT_LENGTH);
			this.len = Integer.parseInt(contentLenHeader.getValue());
		}
		
		@Override
		public XFuture<Void> processPiece(StreamMsg data) {
			closeCheck(data);
			if(!(data instanceof DataFrame))
				throw new UnsupportedOperationException("not supported in http1.1="+data);
			
			DataFrame frame = (DataFrame) data;
			
			totalWritten += frame.getData().getReadableSize();
			if(totalWritten > len)
				throw new IllegalArgumentException("You wrote more than the content length header="+len+" written size="+totalWritten);
			else if(frame.isEndOfStream() && totalWritten != len)
				throw new IllegalArgumentException("You did not write enough data.  written="+totalWritten+" content length header="+len);
			
			if(frame.isEndOfStream()) {
				log.info(socket+" done sending response2");
				remove(data);
			}

			HttpData httpData = new HttpData(frame.getData(), frame.isEndOfStream());
			return write(httpData).thenApply(c -> {
				if(frame.isEndOfStream())
					permitQueue.releasePermit();
				return null;
			});
		}
	}
	
	private class Http11ChunkedWriter implements StreamWriter {

		private HttpRequest http1Req2;
		private Http2Request headers2;

		public Http11ChunkedWriter(HttpRequest http1Req, Http2Request headers) {
			http1Req2 = http1Req;
			headers2 = headers;
		}

		@Override
		public String toString() {
			return "Http1ChunkedWriter["+headers2.getSingleHeaderValue(Http2HeaderName.PATH)+"]["+socket+"]";
		}
		
		@Override
		public XFuture<Void> processPiece(StreamMsg data) {
			closeCheck(data);
			if(!(data instanceof DataFrame))
				throw new UnsupportedOperationException("not supported in http1.1="+data);
			DataFrame frame = (DataFrame) data;

			
			//MULTIPLE scenarios here.
			//1. Someone sends StreamMsg with isEndOfStream=true  WITH data -> send HttpChunk AND HttpLastChunk
			//2. Someone sends StreamMsg with isEndOfStream=true  WITH NO data -> send HttpLastChunk
			//3. Someone sends StreamMsg with isEndOfStream=false WITH data -> send HttpChunk
			//4. Someone sends StreamMsg with isEndOfStream=false WITH NO data -> exception...make clients fix their bugs
			if(!frame.isEndOfStream()) {
				if(frame.getData().getReadableSize() == 0)
					throw new IllegalArgumentException("DataFrame must contain data if isEndOfStream is false");
				return write(new HttpChunk(frame.getData()));
			}

			XFuture<Void> future = XFuture.completedFuture(null);
			if(frame.getData().getReadableSize() > 0)
				future = write(new HttpChunk(frame.getData()));
			
			remove(data);	

			if(log.isDebugEnabled())
				log.debug(socket+" done sending response");
			future = future.thenCompose(w -> {
				return write(new HttpLastChunk());
			}).thenApply(v -> {
				permitQueue.releasePermit();
				return null;
			});
			
			return future;
		}
	}

	private void remove(Http2Msg data) {
		Http11StreamImpl current = socket.getCurrentStream();
		if(endingFrame.get() != null)
			throw new IllegalStateException("You had already sent a frame with endOfStream "
					+ "set and can't send more.  ending frame was="+endingFrame+" but you just sent="+data);
		else if(current != this)
			throw new IllegalStateException("Due to http1.1 spec, YOU MUST return "
					+ "responses in order and this is not the current response that needs responding to");

		endingFrame.set(data);
		socket.setCurrentStream(null);
		
	}
	
	private XFuture<Void> write(HttpPayload payload) {
		if(hasRespondedToConnect) {
			HttpChunk chunk = payload.getHttpChunk();
			DataWrapper body = chunk.getBodyNonNull();
			byte[] createByteArray = body.createByteArray();
			ByteBuffer buffer = ByteBuffer.wrap(createByteArray);
			return socket.getChannel().write(buffer);
		}
		
		
		ByteBuffer buf = http11Parser.marshalToByteBuffer(socket.getHttp11MarshalState(), payload);
		if(isForConnectRequeest) {
			hasRespondedToConnect = true;
		}


		try {
			return socket.getChannel().write(buf);
		} catch (NioClosedChannelException e) {
			throw new NioClosedChannelException("payload not written="+payload, e);
		}
	}
	
	@Override
	public PushStreamHandle openPushStream() {
		throw new UnsupportedOperationException("not supported for http1.1 requests");
	}

	@Override
	public XFuture<Void> cancel(CancelReason reason) {
		return socket.getChannel().close();
	}

	@Override
	public FrontendSocket getSocket() {
		return socket;
	}

	@Override
	public Map<String, Object> getSession() {
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

	public void setSentFullRequest(boolean sent) {
		this.sentFullRequest = sent;
	}

	public boolean isForConnectRequeest() {
		return isForConnectRequeest;
	}

	public void setStreamRef(StreamRef streamRef) {
		this.streamRef = streamRef;
	}

	public StreamRef getStreamRef() {
		return streamRef;
	}
	
}
