package org.webpieces.httpfrontend.api;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.frontend.api.*;
import org.webpieces.httpcommon.api.HttpSocket;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpparser.api.dto.HttpRequest;

public class IntegTestFrontend {

	public static void main(String[] args) {
		BufferCreationPool pool = new BufferCreationPool();
		HttpFrontendManager frontEndMgr = HttpFrontendFactory.createFrontEnd("frontEnd", 10, null, pool);
		FrontendConfig config = new FrontendConfig("id2", new InetSocketAddress(8080));
		frontEndMgr.createHttpServer(config, new OurListener());
	}
	
	private static class OurListener implements RequestListener {

		@Override
		public void incomingRequest(HttpRequest req, RequestId requestId, boolean isComplete, ResponseSender sender) {
		}

		@Override
		public CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isComplete, ResponseSender sender) {
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
