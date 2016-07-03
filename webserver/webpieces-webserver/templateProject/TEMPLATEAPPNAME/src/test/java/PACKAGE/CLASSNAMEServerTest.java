package PACKAGE;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Module;

public class CLASSNAMEServerTest {
	
	//This is in progress but basically you can provide app overrides to mock remote systems or swap
	//in in-memory systems, etc.
	
	//see below comments in AppOverrideModule
	//private MockRemoteSystem mockRemote = new MockRemoteSystem(); //our your favorite mock library
	
	@Before
	public void setUp() throws InterruptedException {
		new CLASSNAMEServer(null, new AppOverridesModule(), false).start();
		
		//ugh, we need to use platformOverride to insert a mockFrontend or something
		//TBD on that...not a big deal really since we can swap stuff but need to figure out what we want
		//it to look like to make it easy for users(and me!!!!)
		//start method could return a reference that we can fire in requests to?
		//though I especially want a emulator layer that I can call getElementById(xx).click() to write really
		//really fast tests that really test everything
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
