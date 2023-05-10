package org.webpieces.httpfrontend2.api;

import java.util.ArrayList;
import java.util.List;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.TwoPools;
import org.webpieces.frontend2.api.FrontendMgrConfig;
import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.HttpFrontendFactory;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.HttpSvrConfig;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.frontend2.api.StreamListener;
import org.webpieces.httpfrontend2.api.http2.Http2Requests;
import org.webpieces.util.SneakyThrow;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.Http2Method;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class ServerFactory {
	private static final Logger log = LoggerFactory.getLogger(ServerFactory.class);
    static final String MAIN_RESPONSE = "Here's the file";
    static final String PUSHED_RESPONSE = "Here's the css";

    static int createTestServer(boolean alwaysHttp2, Long maxConcurrentStreams) {
        TwoPools pool = new TwoPools("pl", new SimpleMeterRegistry());
        ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("webpieces-timer"));
        FrontendMgrConfig config = new FrontendMgrConfig();
        HttpFrontendManager frontEndMgr = HttpFrontendFactory.createFrontEnd("frontEnd", timer, pool, config, Metrics.globalRegistry);
        HttpSvrConfig svrConfig = new HttpSvrConfig("id2");
        HttpServer server = frontEndMgr.createHttpServer(svrConfig, new OurListener());
        XFuture<Void> fut = server.start();
        try {
			fut.get(2, TimeUnit.SECONDS);
		} catch (ExecutionException | InterruptedException | TimeoutException e) {
			throw SneakyThrow.sneak(e);
		}
        return server.getUnderlyingChannel().getLocalAddress().getPort();
    }

    private static class OurListener implements StreamListener, StreamWriter {
//        private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
//        private HttpRequest pushedRequest = Requests.createRequest(KnownHttpMethod.GET, "/file.css");

		@Override
		public 	HttpStream openStream(FrontendSocket socket) {
			return new StreamHandleImpl();
		}
		
		@Override
		public XFuture<Void> processPiece(StreamMsg data) {
			return XFuture.completedFuture(null);
		}

		@Override
		public void fireIsClosed(FrontendSocket socketThatClosed) {
			
		}
    }
    
    private static class StreamHandleImpl implements HttpStream {

    	private XFuture<StreamWriter> streamWriter = new XFuture<StreamWriter>();
    	
		@Override
		public StreamRef incomingRequest(Http2Request headers, ResponseStream stream) {
			XFuture<StreamWriter> writer = incomingRequestImpl(headers, stream);
			return new MyStremRef(writer);
		}
		
		public XFuture<StreamWriter> incomingRequestImpl(Http2Request headers, ResponseStream stream) {
			log.info(stream+"request="+headers);
			Http2Response response = Http2Requests.createResponse(headers.getStreamId());
			if(headers.getKnownMethod() == Http2Method.HEAD) {
				if(!headers.isEndOfStream())
					log.error("they sent bad headers and we missed it in the engine?");
				response.setEndOfStream(true);
				log.info(stream+"HEAD request creating response from request.  endStream="+response.isEndOfStream());
				return stream.process(response).thenApply(s -> new NullStreamWriter(stream, s));
			} else if(headers.isEndOfStream()) {
				log.info(stream+"EOS creating response from request.  endStream="+response.isEndOfStream());
				return stream.process(response).thenApply(s -> new NullStreamWriter(stream, s));
			}			
			
			log.info(stream+"NOT EOS delaying response.");			

			streamWriter.complete(new CachedResponseWriter(stream, response));
			return streamWriter;
		}

    }

    private static class MyStremRef implements StreamRef {

		private XFuture<StreamWriter> writer;

		public MyStremRef(XFuture<StreamWriter> writer) {
			this.writer = writer;
		}

		@Override
		public XFuture<StreamWriter> getWriter() {
			return writer;
		}

		@Override
		public XFuture<Void> cancel(CancelReason reason) {
			return XFuture.completedFuture(null); //nothing really to do on cancel
		}
    	
    }
    private static class NullStreamWriter implements StreamWriter {

		private ResponseStream stream;
		private StreamWriter writer;

		public NullStreamWriter(ResponseStream stream, StreamWriter writer) {
			this.stream = stream;
			this.writer = writer;
		}

		@Override
		public XFuture<Void> processPiece(StreamMsg data) {
			log.error(stream+"receiving data="+data);
			return XFuture.completedFuture(null);
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
//		public XFuture<Void> processPiece(StreamMsg data) {
//			log.info(stream+"echoing back piece of data");
//			return writer.processPiece(data);
//		}
//    	
//    }
    
    private static class CachedResponseWriter implements StreamWriter {

		private ResponseStream stream;
		private Http2Response response;
		private List<StreamMsg> datas = new ArrayList<>();
		private XFuture<StreamWriter> futureWriter = new XFuture<StreamWriter>();

		public CachedResponseWriter(ResponseStream stream, Http2Response response) {
			this.stream = stream;
			this.response = response;
		}

		@Override
		public XFuture<Void> processPiece(StreamMsg data) {
			if(!data.isEndOfStream()) {
				log.info(stream+"consuming data");
				datas.add(data);
				return XFuture.completedFuture(null);
			}
			
			XFuture<StreamWriter> future = stream.process(response);
			future.thenApply(w -> futureWriter.complete(w));
			
			return future.thenCompose(w -> sendAllDatas(w, datas));
		}
		
    	private XFuture<Void> sendAllDatas(StreamWriter writer, List<StreamMsg> datas2) {
    		XFuture<Void> fut = XFuture.completedFuture(null);
    		for(StreamMsg m : datas2) {
    			fut = fut.thenCompose(v -> writer.processPiece(m));
    		}
    		return fut;
    	}

    }
}
