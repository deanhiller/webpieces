package org.webpieces.webserver.scopes;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.SeleniumOverridesForTest;

public class TestScopesSelenium {
	
	private static WebDriver driver;
	
	private int port;

	@BeforeClass
	public static void staticSetup() {
		driver = new FirefoxDriver();
	}
	
	@AfterClass
	public static void tearDown() {
		driver.close();
		driver.quit();
	}
	
	@Ignore
	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		TemplateCompileConfig config = new TemplateCompileConfig();
		VirtualFileClasspath metaFile = new VirtualFileClasspath("scopesMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new SeleniumOverridesForTest(config), null, true, metaFile);
		webserver.start();
		port = webserver.getUnderlyingHttpChannel().getLocalAddress().getPort();
	}
	
	//You must have firefox installed to run this test...
	@Ignore
	@Test
	public void testSessionBasic() throws ClassNotFoundException {
		driver.get("http://localhost:"+port+"/home");
		
		String pageSource1 = driver.getPageSource();
		Assert.assertTrue("pageSource="+pageSource1, pageSource1.contains("age=30"));

		WebElement theLink = driver.findElement(By.id("link"));
		theLink.click();

		String pageSource2 = driver.getPageSource();
		Assert.assertTrue("pageSource="+pageSource2, pageSource2.contains("age=30"));
		Assert.assertTrue("pageSource="+pageSource2, pageSource2.contains("result=true"));
		Assert.assertTrue("pageSource="+pageSource2, pageSource2.contains("name=Dean"));
	}
	
	@Ignore
	@Test
	public void testSessionTooBig() {
		driver.get("http://localhost:"+port+"/home");
		
		String pageSource = driver.getPageSource();
		Assert.assertTrue("pageSource="+pageSource, pageSource.contains("age=30"));
		
		WebElement link1 = driver.findElement(By.id("sessionTooLarge"));
		link1.click();
		
		String pageSource1 = driver.getPageSource();
		Assert.assertTrue("pageSource="+pageSource1, pageSource1.contains("There was a bug in our software"));
	}
}
