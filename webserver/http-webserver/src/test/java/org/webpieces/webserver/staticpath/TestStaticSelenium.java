package org.webpieces.webserver.staticpath;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.WebserverForTest;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.SeleniumOverridesForTest;

public class TestStaticSelenium {
	
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
	
	@Before
	public void setUp() throws InterruptedException, ClassNotFoundException {
		Asserts.assertWasCompiledWithParamNames("test");
		
		VirtualFileClasspath metaFile = new VirtualFileClasspath("staticMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new SeleniumOverridesForTest(), null, true, metaFile);
		webserver.start();
		port = webserver.getUnderlyingHttpChannel().getLocalAddress().getPort();
	}

	//This is testing gzip chunked compression and that we still render in firefox.
	@Test
	public void testSessionBasic() throws ClassNotFoundException {
		driver.get("http://localhost:"+port+"/public/staticMeta.txt");

		String pageSource1 = driver.getPageSource();
		Assert.assertTrue("pageSource="+pageSource1, pageSource1.contains("org.webpieces.webserver.staticpath.app.StaticMeta"));
	}

}
