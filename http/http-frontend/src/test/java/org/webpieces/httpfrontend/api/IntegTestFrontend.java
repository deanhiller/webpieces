package org.webpieces.httpfrontend.api;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend.api.*;
import org.webpieces.httpcommon.Responses;
import org.webpieces.httpcommon.api.*;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownStatusCode;

public class IntegTestFrontend {

	public static void main(String[] args) throws InterruptedException {
		BufferCreationPool pool = new BufferCreationPool();
		HttpFrontendManager frontEndMgr = HttpFrontendFactory.createFrontEnd("frontEnd", 10, null, pool);
		FrontendConfig config = new FrontendConfig("id2", new InetSocketAddress(8081));
		frontEndMgr.createHttpServer(config, new OurListener());
		synchronized (IntegTestFrontend.class) {
			IntegTestFrontend.class.wait();
		}
	}
	
	private static class OurListener implements RequestListener {
		private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
		private HttpResponse responseA = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.wrapString("Here's the file"));
		private HttpResponse pushedResponse = Responses.createResponse(KnownStatusCode.HTTP_200_OK, dataGen.wrapString("Here's the css"));
		private Map<RequestId, HttpRequest> idMap = new HashMap<>();

		private void sendResponse(RequestId requestId, ResponseSender sender) {
			HttpRequest req = idMap.get(requestId);

			sender.sendResponse(responseA, req, requestId, true);
			if(sender.getProtocol() == Protocol.HTTP2) {
				sender.sendResponse(pushedResponse, req, requestId, true);
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
