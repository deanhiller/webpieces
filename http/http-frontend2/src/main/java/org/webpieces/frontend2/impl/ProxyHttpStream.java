package org.webpieces.frontend2.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.ResponseStream;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.PushStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class ProxyHttpStream implements HttpStream {

	private static final Logger log = LoggerFactory.getLogger(ProxyHttpStream.class);
	
	private HttpStream openStream;

	public ProxyHttpStream(HttpStream openStream) {
		this.openStream = openStream;
	}

	@Override
	public StreamRef incomingRequest(Http2Request request, ResponseStream stream) {
		String expect = request.getSingleHeaderValue("Expect");
		CompletableFuture<StreamWriter> future = CompletableFuture.completedFuture(null); 
		if(expect != null && "100-continue".equals(expect.toLowerCase())) {
			Http2Response continueResponse = new Http2Response();
			continueResponse.setEndOfStream(false);
			continueResponse.addHeader(new Http2Header(Http2HeaderName.STATUS, "100"));
			
			future = stream.process(continueResponse);
		}
		
		//This is only for streaming to backpressure clients IF we responded OR cancelled so we don't
		//waste CPU on a client stream coming in
		CompletableFuture<ProxyWriter> futureWriter = new CompletableFuture<>();
		
		ProxyResponseStream proxy = new ProxyResponseStream(request, stream, futureWriter);
		StreamRef streamRef = openStream.incomingRequest(request, proxy);

		CompletableFuture<StreamWriter> writer = future.thenCompose(w -> {
			return createProxy(streamRef.getWriter(), futureWriter);
		});
		
		return new ProxyStreamRef(writer, streamRef);
	}

	private CompletableFuture<StreamWriter> createProxy(CompletableFuture<StreamWriter> future, CompletableFuture<ProxyWriter> futureWriter) {
		return future.thenApply( w -> {
			ProxyWriter writer = new ProxyWriter(w);
			futureWriter.complete(writer); //fills in the ProxyWriter in the future IF EVER(sometimes it may never be filled in
			return writer;			
		});
	}

	private class ProxyResponseStream implements ResponseStream {

		private ResponseStream stream;
		private CompletableFuture<ProxyWriter> futureWriter;
		private Http2Request request;

		public ProxyResponseStream(Http2Request request, ResponseStream stream, CompletableFuture<ProxyWriter> futureWriter) {
			this.request = request;
			this.stream = stream;
			this.futureWriter = futureWriter;
		}

		public FrontendSocket getSocket() {
			return stream.getSocket();
		}

		public Map<String, Object> getSession() {
			return stream.getSession();
		}

		public CompletableFuture<StreamWriter> process(Http2Response response) {
			//if is end of stream, we don't need any more data from client so backpressure the client
			//This did not work
			//Need test case BUT until then orderly/java/servers/proxy-svc STOPS working on just doing
			//curl --proxy http://localhost:3128 https://caredash.com 
			//though I am not sure why this is the case.  I am not sure why turning off reading is not ok since response is complete and sent
			//NOTE: It is mostly on a response of 301 Moved Permanently response with content length 0 btw!
//			if(response.isEndOfStream()) {
//				String conn = request.getSingleHeaderValue(Http2HeaderName.CONNECTION);
//				if("keep-alive".equals(conn)) {
//					log.info("response end of stream, but is a keep alive connection so no need to turn off reads");
//				} else {
//					log.info("response="+response+" is end of stream and keep-alive not set so turn backpressure on to not read");
//					futureWriter.thenApply( p -> p.turnOnBackpressure());
//				}
//			}
			
			return stream.process(response);
		}

		public PushStreamHandle openPushStream() {
			return stream.openPushStream();
		}

		public CompletableFuture<Void> cancel(CancelReason reason) {
			log.info("cancelling stream. reason="+reason);
			
			//if response cancels, swap StreamWriter to BackupStream so we backpressure FAST and then don't
			//need to use CPU after that
			futureWriter.thenApply( p -> p.turnOnBackpressure());
			
			return stream.cancel(reason);
		}
	}
	
	private class ProxyStreamRef implements StreamRef {
		private StreamRef streamRef;
		private CompletableFuture<StreamWriter> writer;

		public ProxyStreamRef(CompletableFuture<StreamWriter> writer, StreamRef streamRef) {
			this.writer = writer;
			this.streamRef = streamRef;
		}

		@Override
		public CompletableFuture<StreamWriter> getWriter() {
			return writer;
		}

		@Override
		public CompletableFuture<Void> cancel(CancelReason reason) {
			return streamRef.cancel(reason);
		}
	}
	
	private class ProxyWriter implements StreamWriter {

		private volatile boolean backPressure;
		private StreamWriter writer;

		public ProxyWriter(StreamWriter writer) {
			this.writer = writer;
		}

		public Void turnOnBackpressure() {
			backPressure = true;
			return null;
		}

		@Override
		public CompletableFuture<Void> processPiece(StreamMsg data) {
			if(backPressure) {
				log.info("Backpressure on222, NOT consuming this data on purpose so socket will deregister.  This is to avoid wasted CPU on data you don't plan on consuming.");
				return new CompletableFuture<Void>(); //unresolved future will kcik in backpressure AND CPU will stop being used on deregister socket
			}
			return writer.processPiece(data);
		}
		
	}
	
}
