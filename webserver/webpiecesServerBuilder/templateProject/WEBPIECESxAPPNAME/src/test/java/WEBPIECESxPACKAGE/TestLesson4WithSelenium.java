package WEBPIECESxPACKAGE;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
//import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.webpieces.ddl.api.JdbcApi;
import org.webpieces.ddl.api.JdbcConstants;
import org.webpieces.ddl.api.JdbcFactory;
import org.webpieces.plugins.hibernate.HibernatePlugin;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.SeleniumOverridesForTest;

import com.google.inject.Binder;
import com.google.inject.Module;

public class TestLesson4WithSelenium {
	
	private static WebDriver driver;
	private JdbcApi jdbc = JdbcFactory.create(JdbcConstants.jdbcUrl, JdbcConstants.jdbcUser, JdbcConstants.jdbcPassword);
	private static String pUnit = HibernatePlugin.PERSISTENCE_TEST_UNIT;

	//see below comments in AppOverrideModule
	//private MockRemoteSystem mockRemote = new MockRemoteSystem(); //our your favorite mock library
	
	private int port;
	private int httpsPort;

	@BeforeClass
	public static void staticSetup() {
		driver = new FirefoxDriver();
	}
	@AfterClass
	public static void staticTearDown() {	
		driver.close();
		driver.quit();
	}
	
	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		jdbc.dropAllTablesFromDatabase();
		
		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test AND you can no longer run single threaded(tradeoffs, tradeoffs)
		//This is however pretty fast to do in many systems...
		Server webserver = new Server(
				new SeleniumOverridesForTest(), new AppOverridesModule(), new ServerConfig(0, 0, pUnit));
		webserver.start();
		port = webserver.getUnderlyingHttpChannel().getLocalAddress().getPort();
		httpsPort = webserver.getUnderlyingHttpsChannel().getLocalAddress().getPort();
	}
	
	@After
	public void tearDown() {
		Options manage = driver.manage();
		manage.deleteAllCookies();
	}
	
	//You must have firefox 47.0.1 installed to run this test!!!!
	//@Ignore
	@Test
	public void testRedirectToOriginallyRequestedUrlAfterLogin() throws ClassNotFoundException {

		driver.navigate().to("https://localhost:"+httpsPort+"/secure/crud/user/list");
		
		Assert.assertEquals("https://localhost:"+httpsPort+"/login", driver.getCurrentUrl());
		
		driver.findElement(By.id("user")).sendKeys("bob");
		driver.findElement(By.id("submit")).click();

		String text = driver.findElement(By.id("errorDiv")).getText();
		Assert.assertTrue(text.contains("No Soup for you!"));
		String errorMsg = driver.findElement(By.id("username_errorMsg")).getText();
		Assert.assertTrue(errorMsg.contains("I lied, Username must be 'dean'"));
		
		WebElement userInput = driver.findElement(By.id("user"));
		userInput.clear();
		userInput.sendKeys("dean");
		
		driver.findElement(By.id("submit")).click();
		
		//ensure it redirects back to originally requested url...
		Assert.assertEquals("https://localhost:"+httpsPort+"/secure/crud/user/list", driver.getCurrentUrl());
	}
	
	//@Ignore
	@Test
	public void testBasicLogin() throws ClassNotFoundException {

		driver.navigate().to("https://localhost:"+httpsPort+"/login");
				
		driver.findElement(By.id("user")).sendKeys("dean");
		driver.findElement(By.id("submit")).click();

		//ensure we redirect to logged in base home page
		Assert.assertEquals("https://localhost:"+httpsPort+"/secure/loggedinhome", driver.getCurrentUrl());
	}
	
	//You must have firefox installed to run this test...
	//@Ignore
	@Test
	public void testChunking() throws ClassNotFoundException {

		//We know this web page is big enough to test our chunking compatibility with a real browser
		driver.get("http://localhost:"+port+"");
		
		String pageSource = driver.getPageSource();
		
		Assert.assertTrue("pageSource="+pageSource, pageSource.contains("Webpieces Webserver is the most"));
		
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
