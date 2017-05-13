package org.webpieces.httpfrontend2.api;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend2.api.FrontendConfig;
import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.api.HttpFrontendFactory;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.frontend2.api.Protocol;
import org.webpieces.httpfrontend2.api.http1.Requests;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.RstStreamFrame;

class ServerFactory {
    static final String MAIN_RESPONSE = "Here's the file";
    static final String PUSHED_RESPONSE = "Here's the css";

    static int createTestServer(boolean alwaysHttp2, Long maxConcurrentStreams) {
        BufferCreationPool pool = new BufferCreationPool();
        ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("webpieces-timer"));
        HttpFrontendManager frontEndMgr = HttpFrontendFactory.createFrontEnd("frontEnd", 10, timer, pool);
        FrontendConfig config = new FrontendConfig("id2", new InetSocketAddress(0));
        // Set this to true to test with h2spec
        config.alwaysHttp2 = alwaysHttp2;
        HttpServer server = frontEndMgr.createHttpServer(config, new OurListener());
        server.start();
        return server.getUnderlyingChannel().getLocalAddress().getPort();
    }

    private static class OurListener implements HttpRequestListener {
        private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
        private HttpRequest pushedRequest = Requests.createRequest(KnownHttpMethod.GET, "/file.css");
        private Map<Long, HttpRequest> idMap = new HashMap<>();

        private void sendResponse(Long requestId, FrontendSocket sender) {
            HttpRequest req = idMap.get(requestId);

            if(req.getRequestLine().getMethod().getMethodAsString().equals("HEAD")) {
//                sender.sendResponse(responseANoBody, req, requestId, true);
            } else {
//                sender.sendResponse(responseA, req, requestId, true);
            }

//            if(sender.getProtocol() == Protocol.HTTP2) {
//                sender.sendResponse(pushedResponse, pushedRequest, requestId, true);
//            }
        }

		@Override
		public StreamWriter incomingRequest(FrontendStream stream, Http2Headers headers, Protocol type) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void cancelRequest(FrontendStream stream, RstStreamFrame c) {
			// TODO Auto-generated method stub
			
		}

    }
}
