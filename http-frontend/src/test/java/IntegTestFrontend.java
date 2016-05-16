import org.webpieces.httpproxy.api.HttpFrontendManager;
import org.webpieces.httpproxy.api.HttpRequestListener;
import org.webpieces.nio.api.channels.Channel;

import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.KnownStatusCode;

import java.net.InetSocketAddress;

import org.webpieces.httpproxy.api.HttpFrontendFactory;

public class IntegTestFrontend {

	public static void main(String[] args) {
		HttpFrontendManager frontEndMgr = HttpFrontendFactory.createFrontEnd("frontEnd", 10);
		
		frontEndMgr.createHttpServer("id2", new InetSocketAddress(8080), new OurListener());
	}
	
	private static class OurListener implements HttpRequestListener {

		@Override
		public void sendServerResponse(Channel channel, Throwable exc, KnownStatusCode http500) {
		}

		@Override
		public void clientClosedChannel(Channel channel) {
		}

		@Override
		public void applyWriteBackPressure(Channel channel) {
		}

		@Override
		public void releaseBackPressure(Channel channel) {
		}

		@Override
		public void processHttpRequests(Channel channel, HttpRequest req) {
		}
		
	}
}
