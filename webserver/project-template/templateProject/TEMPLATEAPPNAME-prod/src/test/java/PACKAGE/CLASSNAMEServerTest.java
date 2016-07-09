package PACKAGE;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.webserver.test.PlatformOverridesForTest;

import com.google.inject.Binder;
import com.google.inject.Module;

public class CLASSNAMEServerTest {
	
	//see below comments in AppOverrideModule
	//private MockRemoteSystem mockRemote = new MockRemoteSystem(); //our your favorite mock library
	
	private HttpRequestListener server;

	@Before
	public void setUp() throws InterruptedException {
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run single threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		CLASSNAMEServer webserver = new CLASSNAMEServer(new PlatformOverridesForTest(), new AppOverridesModule(), false);
		server = webserver.start();
	}
	
	@Test
	public void testSomething() {
		
	}
	
	private class AppOverridesModule implements Module {
		@Override
		public void configure(Binder binder) {
			//Add overrides here generally using mocks from fields in the test class
			
			//ie.
			//binder.bind(SomeRemoteSystem.class).toInstance(mockRemote); //see above comment on the field mockRemote
		}
	}
	
}
