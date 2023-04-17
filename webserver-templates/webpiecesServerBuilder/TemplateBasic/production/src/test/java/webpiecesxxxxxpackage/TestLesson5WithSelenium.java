package webpiecesxxxxxpackage;

import java.awt.AWTException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.inject.util.Modules;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.webpieces.ddl.api.JdbcApi;
import org.webpieces.ddl.api.JdbcConstants;
import org.webpieces.ddl.api.JdbcFactory;
import org.webpieces.webserver.api.ServerConfig;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.EnvSimModule;
import org.webpieces.webserver.test.OverridesForTestRealServer;

import com.google.inject.Binder;
import com.google.inject.Module;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import webpiecesxxxxxpackage.mock.JavaCache;

//import org.junit.Ignore;

/**
 * If you install firefox 47.0.1, these tests will just work out of the box
 * Until then, we mark this test as ignored for you
 * 
 * @author dhiller
 */
//@Ignore
public class TestLesson5WithSelenium {
	
	private static WebDriver driver;
	private JdbcApi jdbc = JdbcFactory.create(JdbcConstants.jdbcUrl, JdbcConstants.jdbcUser, JdbcConstants.jdbcPassword);
	private String[] args = { "-http.port=:0", "-https.port=:0", "-hibernate.persistenceunit=webpiecesxxxxxpackage.db.DbSettingsInMemory", "-hibernate.loadclassmeta=true"};

	private Map<String, String> simulatedEnv = Map.of(
			"REQ_ENV_VAR", "somevalue"
	);

	//see below comments in AppOverrideModule
	//private MockRemoteSystem mockRemote = new MockRemoteSystem(); //or your favorite mock library
	
	private int httpPort;
	private int httpsPort;
	private SimpleMeterRegistry metrics = new SimpleMeterRegistry();

	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException {
		driver = new FirefoxDriver();
		
		Asserts.assertWasCompiledWithParamNames("test");
		
		jdbc.dropAllTablesFromDatabase();

		Module overrides = Modules.combine(
				new OverridesForTestRealServer(metrics),
				new EnvSimModule(simulatedEnv)
		);

		//you may want to create this server ONCE in a static method BUT if you do, also remember to clear out all your
		//mocks after every test and NOT drop tables but clear and re-populate
		Server webserver = new Server(
				overrides, new AppOverridesModule(),
				new ServerConfig(JavaCache.getCacheLocation()), args);
		
		webserver.start();
		httpPort = webserver.getUnderlyingHttpChannel().getLocalAddress().getPort();
		httpsPort = webserver.getUnderlyingHttpsChannel().getLocalAddress().getPort();
	}
	
	@After
	public void tearDown() {
		Options manage = driver.manage();
		manage.deleteAllCookies();
		
		driver.close();
		driver.quit();
	}
	
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
	
	@Test
	public void testAjaxRedirect() throws ClassNotFoundException {

		driver.navigate().to("https://localhost:"+httpsPort+"/secure/ajax/user/list");		
		Assert.assertEquals("https://localhost:"+httpsPort+"/login", driver.getCurrentUrl());
		
		driver.findElement(By.id("user")).sendKeys("dean");
		driver.findElement(By.id("submit")).click();
		
		Assert.assertEquals("https://localhost:"+httpsPort+"/secure/ajax/user/list", driver.getCurrentUrl());
		
		//open new tab and logout
		openNewTabAndLogoutAndComeBackToThisTab();
		
		driver.findElement(By.id("editLink_2")).click();

		//redirected to login page with ajax redirect
		Assert.assertEquals("https://localhost:"+httpsPort+"/login", driver.getCurrentUrl());
	}
	
	@Test
	public void testBasicLogin() throws ClassNotFoundException {

		driver.navigate().to("https://localhost:"+httpsPort+"/login");
				
		driver.findElement(By.id("user")).sendKeys("dean");
		driver.findElement(By.id("submit")).click();

		//ensure we redirect to logged in base home page
		Assert.assertEquals("https://localhost:"+httpsPort+"/secure/loggedinhome", driver.getCurrentUrl());
		
		//now if we navigate to login page, it automatically redirects us to the loggidinhome page since we are already logged in
		driver.navigate().to("https://localhost:"+httpsPort+"/login");
		
		Assert.assertEquals("https://localhost:"+httpsPort+"/secure/loggedinhome", driver.getCurrentUrl());		
	}
	
	/**
	 * VERY VERY IMPORTANT SECURITY TEST testing that back button cannot go back and see logged in pages when logged
	 * out.  This makes sure we tell browser pages are cached.  This should be extended to more browsers as browsers in
	 * the past have varied in their behavior here
	 */
	@Test
	public void testBackButtonToSecurePageWhenLoggedOut() throws ClassNotFoundException, AWTException {

		driver.navigate().to("https://localhost:"+httpsPort+"/login");
				
		driver.findElement(By.id("user")).sendKeys("dean");
		driver.findElement(By.id("submit")).click();

		//ensure we redirect to logged in base home page
		Assert.assertEquals("https://localhost:"+httpsPort+"/secure/loggedinhome", driver.getCurrentUrl());

		driver.navigate().to("https://localhost:"+httpsPort+"/secure/crud/user/list");

		openNewTabAndLogoutAndComeBackToThisTab();
		
		driver.navigate().back(); //back button
		Assert.assertEquals("https://localhost:"+httpsPort+"/login", driver.getCurrentUrl());
	}
	
	private void openNewTabAndLogoutAndComeBackToThisTab() {
		WebElement link = driver.findElement(By.id("loggedinhome"));
		new Actions(driver)
	    .keyDown(Keys.CONTROL)
	    .keyDown(Keys.SHIFT)
	    .click(link)
	    .keyUp(Keys.SHIFT)
	    .keyUp(Keys.CONTROL)
	    .perform();
		
		//open new tab
	    driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"t");
	    List<String> tabs = new ArrayList<String> (driver.getWindowHandles());
	    driver.switchTo().window(tabs.get(1));
	    
		driver.navigate().to("https://localhost:"+httpsPort+"/secure/loggedinhome");
		
		
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		
		driver.findElement(By.id("logout")).click();
		driver.switchTo().window(tabs.get(0));
		
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
	}
	
	@Test
	public void testChunking() throws ClassNotFoundException {

		//We know this web page is big enough to test our chunking compatibility with a real browser
		driver.get("http://localhost:"+httpPort+"");
		
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
