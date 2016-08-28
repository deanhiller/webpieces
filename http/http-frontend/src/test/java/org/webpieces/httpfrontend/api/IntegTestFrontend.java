package org.webpieces.httpfrontend.api;
import java.net.InetSocketAddress;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.frontend.api.exception.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;

public class IntegTestFrontend {

	public static void main(String[] args) {
		BufferCreationPool pool = new BufferCreationPool();
		HttpFrontendManager frontEndMgr = HttpFrontendFactory.createFrontEnd("frontEnd", 10, null, pool);
		FrontendConfig config = new FrontendConfig("id2", new InetSocketAddress(8080));
		frontEndMgr.createHttpServer(config, new OurListener());
	}
	
	private static class OurListener implements HttpRequestListener {

		@Override
		public void processHttpRequests(FrontendSocket channel, HttpRequest req, boolean isHttps) {
		}

		@Override
		public void sendServerResponse(FrontendSocket channel, HttpException exc) {
		}

		@Override
		public void clientOpenChannel(FrontendSocket channel) {
		}
		
		@Override
		public void clientClosedChannel(FrontendSocket channel) {
		}

		@Override
		public void applyWriteBackPressure(FrontendSocket channel) {
		}

		@Override
		public void releaseBackPressure(FrontendSocket channel) {
		}
		
	}
}
