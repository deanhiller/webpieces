package org.webpieces.webserver.basic;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.OverridesForTestRealServer;

import com.google.inject.Binder;
import com.google.inject.Module;

public class SeleniumBasicTest {
	
	private static WebDriver driver;
	
	private int port;

	@BeforeClass
	public static void staticSetup() {
		try {
		driver = new FirefoxDriver();
		} catch(Throwable e) {
			throw new RuntimeException("You must have firefox 47.0 :(", e);
		}
	}
	@AfterClass
	public static void tearDown() {
		driver.close();
		driver.quit();
	}
	
	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run multi-threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		WebserverForTest webserver = new WebserverForTest(new OverridesForTestRealServer(), new AppOverridesModule(), true, null);
		webserver.start();
		port = webserver.getUnderlyingHttpChannel().getLocalAddress().getPort();
	}
	

	
	//You must have firefox installed to run this test...
	//@Ignore
	@Test
	public void testSomething() throws ClassNotFoundException {

		driver.get("http://localhost:"+port);
		
		String pageSource = driver.getPageSource();
		
		Assert.assertTrue("pageSource="+pageSource, pageSource.contains("This is the first raw html page"));
		
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
