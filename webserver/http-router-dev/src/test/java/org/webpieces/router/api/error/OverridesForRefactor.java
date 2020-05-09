package org.webpieces.router.api.error;

import org.webpieces.router.api.ResponseStreamer;

import com.google.inject.Binder;
import com.google.inject.Module;

/** 
	 * Need to live test with browser to see if PRG is better or just returning 404 is better!!!
	 * Current behavior is to return a 404
	 */
	//TODO: Test this with browser and then fix for best user experience
//	@Test
//	public void testNotFoundPostRouteResultsInRedirectToNotFoundCatchAllController() {
//		log.info("starting");
//		String moduleFileContents = CommonRoutesModules.class.getName();
//		RoutingService server = createServer(isProdTest, moduleFileContents);
//		
//		server.start();
//		
//		RouterRequest req = RequestCreation.createHttpRequest(HttpMethod.POST, "/notexistpostroute");
//		MockResponseStream mockResponseStream = new MockResponseStream();
//		
//		server.incomingCompleteRequest(req, mockResponseStream);
//
//		verifyNotFoundRendered(mockResponseStream);
//	}
	
	public class OverridesForRefactor implements Module {

		private ResponseStreamer mock;

		public OverridesForRefactor(ResponseStreamer mock) {
			this.mock = mock;
		}

		@Override
		public void configure(Binder binder) {
			binder.bind(ResponseStreamer.class).toInstance(mock);
		}
		
	}