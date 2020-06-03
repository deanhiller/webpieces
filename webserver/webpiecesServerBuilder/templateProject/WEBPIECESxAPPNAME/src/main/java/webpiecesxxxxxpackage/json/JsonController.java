package webpiecesxxxxxpackage.json;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.plugins.json.Jackson;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.api.exceptions.NotFoundException;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.ResponseStreamHandle;
import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Singleton
public class JsonController implements ClientApi {
	
	private static final Logger log = LoggerFactory.getLogger(JsonController.class);

	private Counter counter;

	@Inject
	public JsonController(MeterRegistry metrics) {
		counter = metrics.counter("testCounter");
	}
	
	public CompletableFuture<SearchResponse> asyncJsonRequest(int id, @Jackson SearchRequest request) {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(8);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return CompletableFuture.completedFuture(resp);
	}
	
	public SearchResponse jsonRequest(int id, @Jackson SearchRequest request) {
		counter.increment();
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(5);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return resp;
	}
	
	@Override
	public SearchResponse postJson(@Jackson SearchRequest request) {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(99);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return resp;
	}
	
	public CompletableFuture<SearchResponse> postAsyncJson(int id, @Jackson SearchRequest request) {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(98);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return CompletableFuture.completedFuture(resp);
	}
	
	@Jackson
	public SearchResponse readOnly() {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(1);
		return resp;
	}
	
	public SearchResponse throwNotFound(int id, @Jackson SearchRequest request) {
		throw new NotFoundException("to test it out");
	}
	
	//Method signature cannot have RequestContext since in microservices, we implement an api as the server
	//AND a client implements the same api AND client does not have a RequestContext!!
	@Override
	public StreamRef myStream(ResponseStreamHandle handle2) {
		RouterStreamHandle handle  = (RouterStreamHandle) handle2;
		RequestContext requestCtx = Current.getContext(); 
		
		Http2Response response = handle.createBaseResponse(requestCtx.getRequest().originalRequest, "text/plain", 200, "Ok");
		response.setEndOfStream(false);
		
		CompletableFuture<StreamWriter> responseWriter = handle.process(response);
		return new RequestStreamEchoWriter(requestCtx, handle, responseWriter);
	}

	private static class RequestStreamEchoWriter implements StreamWriter, StreamRef {

		private AtomicInteger total = new AtomicInteger();
		private CompletableFuture<StreamWriter> responseWriter;
		private RouterStreamHandle handle; // in case you want to cancel the request

		public RequestStreamEchoWriter(RequestContext requestCtx, RouterStreamHandle handle,
				CompletableFuture<StreamWriter> responseWriter2) {
			this.responseWriter = responseWriter2;
			this.handle = handle;
		}

		@Override
		public CompletableFuture<Void> processPiece(StreamMsg data) {
			RequestContext requestCtx = Current.getContext(); 

			DataFrame f = (DataFrame) data;
			int numReceived = total.addAndGet(f.getData().getReadableSize());
			log.info("Num bytes received so far="+numReceived+" for requests="+requestCtx.getRequest().relativePath);
			
			if(data.isEndOfStream()) {
				log.info("Upload complete");
				return responseWriter.thenCompose(w -> w.processPiece(data));
			}

			//We just echo data back to whatever the client sent as the client sends it...
			return responseWriter.thenCompose(w -> w.processPiece(data));
		}

		@Override
		public CompletableFuture<StreamWriter> getWriter() {
			//let's make it wait for our response to be written by 
			//chaining with responseWriter future here
			return responseWriter.thenApply(s -> this);
		}

		@Override
		public CompletableFuture<Void> cancel(CancelReason reason) {
			//here if using http client, we may forward to next stream like so
			//responseStream.cancel(reason);
			//but since the responseStream and request is the same, we can just stop sending instead
			//which happens automatically since they stopped sending(ie. nothing to do here
			return CompletableFuture.completedFuture(null);
		}

	}
}
