import java.net.InetSocketAddress;

import org.webpieces.httpproxy.api.FrontendSocket;
import org.webpieces.httpproxy.api.HttpFrontendFactory;
import org.webpieces.httpproxy.api.HttpFrontendManager;
import org.webpieces.httpproxy.api.HttpRequestListener;

import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.KnownStatusCode;

public class IntegTestFrontend {

	public static void main(String[] args) {
		HttpFrontendManager frontEndMgr = HttpFrontendFactory.createFrontEnd("frontEnd", 10);
		
		frontEndMgr.createHttpServer("id2", new InetSocketAddress(8080), new OurListener());
	}
	
	private static class OurListener implements HttpRequestListener {

		@Override
		public void processHttpRequests(FrontendSocket channel, HttpRequest req) {
		}

		@Override
		public void sendServerResponse(FrontendSocket channel, Throwable exc, KnownStatusCode status) {
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
