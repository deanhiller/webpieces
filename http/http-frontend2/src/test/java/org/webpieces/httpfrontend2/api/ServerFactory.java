package org.webpieces.httpfrontend2.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.frontend2.api.FrontendMgrConfig;
import org.webpieces.frontend2.api.HttpFrontendFactory;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.HttpSvrConfig;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.frontend2.api.StreamListener;
import org.webpieces.httpfrontend2.api.http2.Http2Requests;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.Http2Method;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

class ServerFactory {
	private static final Logger log = LoggerFactory.getLogger(ServerFactory.class);
    static final String MAIN_RESPONSE = "Here's the file";
    static final String PUSHED_RESPONSE = "Here's the css";

    static int createTestServer(boolean alwaysHttp2, Long maxConcurrentStreams) {
        BufferCreationPool pool = new BufferCreationPool();
        ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("webpieces-timer"));
        FrontendMgrConfig config = new FrontendMgrConfig();
        HttpFrontendManager frontEndMgr = HttpFrontendFactory.createFrontEnd("frontEnd", timer, pool, config);
        HttpSvrConfig svrConfig = new HttpSvrConfig("id2");
        HttpServer server = frontEndMgr.createHttpServer(svrConfig, new OurListener());
        CompletableFuture<Void> fut = server.start();
        try {
			fut.get(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException("exception", e);
		} catch (ExecutionException e) {
			throw new RuntimeException("exception", e);
		} catch (TimeoutException e) {
			throw new RuntimeException("exception", e);
		}
        return server.getUnderlyingChannel().getLocalAddress().getPort();
    }

    private static class OurListener implements StreamListener, StreamWriter {
//        private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
//        private HttpRequest pushedRequest = Requests.createRequest(KnownHttpMethod.GET, "/file.css");

		@Override
		public 	HttpStream openStream() {
			return new StreamHandleImpl();
		}
		
		@Override
		public CompletableFuture<Void> processPiece(StreamMsg data) {
			return CompletableFuture.completedFuture(null);
		}

    }
    
    private static class StreamHandleImpl implements HttpStream {

		@Override
		public CompletableFuture<StreamWriter> incomingRequest(Http2Request headers, ResponseStream stream) {
			log.info(stream+"request="+headers);
			Http2Response response = Http2Requests.createResponse(headers.getStreamId());
			if(headers.getKnownMethod() == Http2Method.HEAD) {
				if(!headers.isEndOfStream())
					log.error("they sent bad headers and we missed it in the engine?");
				response.setEndOfStream(true);
				log.info(stream+"HEAD request creating response from request.  endStream="+response.isEndOfStream());
				return stream.sendResponse(response).thenApply(s -> new NullStreamWriter(stream));
			} else if(headers.isEndOfStream()) {
				log.info(stream+"EOS creating response from request.  endStream="+response.isEndOfStream());
				return stream.sendResponse(response).thenApply(s -> new NullStreamWriter(stream));
			}			
			
			log.info(stream+"NOT EOS delaying response.");			

			return CompletableFuture.completedFuture(new CachedResponseWriter(stream, response));
		}

		@Override
		public CompletableFuture<Void> incomingCancel(CancelReason reset) {
			return CompletableFuture.completedFuture(null);
		}
    }

    private static class NullStreamWriter implements StreamWriter {

		private ResponseStream stream;

		public NullStreamWriter(ResponseStream stream) {
			this.stream = stream;
		}

		@Override
		public CompletableFuture<Void> processPiece(StreamMsg data) {
			log.error(stream+"receiving data="+data);
			return CompletableFuture.completedFuture(null);
		}
    }

//    private static class AppStreamWriter implements StreamWriter {
//
//
//		private ResponseStream stream;
//		private StreamWriter writer;
//
//		public AppStreamWriter(ResponseStream stream, StreamWriter writer) {
//			this.stream = stream;
//			this.writer = writer;
//		}
//
//		@Override
//		public CompletableFuture<Void> processPiece(StreamMsg data) {
//			log.info(stream+"echoing back piece of data");
//			return writer.processPiece(data);
//		}
//    	
//    }
    
    private static class CachedResponseWriter implements StreamWriter {

		private ResponseStream stream;
		private Http2Response response;
		private List<StreamMsg> datas = new ArrayList<>();

		public CachedResponseWriter(ResponseStream stream, Http2Response response) {
			this.stream = stream;
			this.response = response;
		}

		@Override
		public CompletableFuture<Void> processPiece(StreamMsg data) {
			if(!data.isEndOfStream()) {
				log.info(stream+"consuming data");
				datas.add(data);
				return CompletableFuture.completedFuture(null);
			}
			
			CompletableFuture<StreamWriter> future = stream.sendResponse(response);
			return future.thenCompose(w -> sendAllDatas(w, datas));
		}
		
    	private CompletableFuture<Void> sendAllDatas(StreamWriter writer, List<StreamMsg> datas2) {
    		CompletableFuture<Void> fut = CompletableFuture.completedFuture(null);
    		for(StreamMsg m : datas2) {
    			fut = fut.thenCompose(v -> writer.processPiece(m));
    		}
    		return fut;
    	}
    }
}
