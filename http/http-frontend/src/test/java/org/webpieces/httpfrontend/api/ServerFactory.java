package org.webpieces.httpfrontend.api;

import com.webpieces.http2parser.api.dto.HasHeaderFragment;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.frontend.api.HttpServer;
import org.webpieces.httpcommon.Requests;
import org.webpieces.httpcommon.Responses;
import org.webpieces.httpcommon.api.*;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.threading.NamedThreadFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

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
        config.maxConcurrentStreams = Optional.of(maxConcurrentStreams);
        HttpServer server = frontEndMgr.createHttpServer(config, new OurListener());
        return server.getUnderlyingChannel().getLocalAddress().getPort();
    }

    private static class OurListener implements RequestListener {
        private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
        private HttpResponse responseA = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.wrapString(MAIN_RESPONSE));
        private HttpResponse responseANoBody = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.emptyWrapper());

        private HttpResponse pushedResponse = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.wrapString(PUSHED_RESPONSE));
        private HttpRequest pushedRequest = Requests.createRequest(KnownHttpMethod.GET, "/file.css");
        private Map<RequestId, HttpRequest> idMap = new HashMap<>();

        private void sendResponse(RequestId requestId, ResponseSender sender) {
            HttpRequest req = idMap.get(requestId);

            if(req.getRequestLine().getMethod().getMethodAsString().equals("HEAD")) {
                sender.sendResponse(responseANoBody, req, requestId, true);
            } else {
                sender.sendResponse(responseA, req, requestId, true);
            }

            if(sender.getProtocol() == Protocol.HTTP2) {
                sender.sendResponse(pushedResponse, pushedRequest, requestId, true);
            }
        }
        @Override
        public void incomingRequest(HttpRequest req, RequestId requestId, boolean isComplete, ResponseSender sender) {
            idMap.put(requestId, req);
            if(isComplete) {
                sendResponse(requestId, sender);
            }

        }

        @Override
        public CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isComplete, ResponseSender sender) {
            if(isComplete) {
                sendResponse(id, sender);
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void incomingTrailer(List<HasHeaderFragment.Header> headers, RequestId id, boolean isComplete, ResponseSender sender) {
            if(isComplete) {
                sendResponse(id, sender);
            }
        }

        @Override
        public void incomingError(HttpException exc, HttpSocket channel) {
        }

        @Override
        public void clientOpenChannel(HttpSocket HttpSocket) {
        }

        @Override
        public void clientClosedChannel(HttpSocket httpSocket) {
        }

        @Override
        public void applyWriteBackPressure(ResponseSender sender) {
        }

        @Override
        public void releaseBackPressure(ResponseSender sender) {
        }

    }
}
