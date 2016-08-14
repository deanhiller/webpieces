package org.webpieces.webserver.beans;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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

import com.google.inject.Binder;
import com.google.inject.Module;

public class TestPRGSelenium {
	
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
		
		TemplateCompileConfig config = new TemplateCompileConfig(WebserverForTest.CHAR_SET_TO_USE);
		VirtualFileClasspath metaFile = new VirtualFileClasspath("beansMeta.txt", WebserverForTest.class.getClassLoader());
		WebserverForTest webserver = new WebserverForTest(new SeleniumOverridesForTest(config), new AppOverridesModule(), true, metaFile);
		webserver.start();
		port = webserver.getUnderlyingHttpChannel().getLocalAddress().getPort();
	}
	

	
	//You must have firefox installed to run this test...
	//@Ignore
	@Test
	public void testSomething() throws ClassNotFoundException {
		driver.get("http://localhost:"+port+"/listusers");
		
		String pageSource = driver.getPageSource();
		Assert.assertTrue("pageSource="+pageSource, pageSource.contains("Add User"));

		WebElement element = driver.findElement(By.id("addUser"));
		element.click();
		
		WebElement userInput = driver.findElement(By.name("user.firstName"));
		userInput.sendKeys("Dean Hiller");
		
		WebElement zipCodeInput = driver.findElement(By.name("user.address.zipCode"));
		zipCodeInput.sendKeys("Text instead of number");		

		WebElement passwordElem = driver.findElement(By.name("password"));
		passwordElem.sendKeys("SomePassword");	
		
		Assert.assertEquals("http://localhost:"+port+"/adduser", driver.getCurrentUrl());
		
		WebElement submit = driver.findElement(By.id("submit"));
		submit.submit();

		//ensure flash scope worked and what the user typed in is still there
		userInput = driver.findElement(By.name("user.firstName"));
		String userName = userInput.getAttribute("value");
		Assert.assertEquals("Dean Hiller", userName);
		
		passwordElem = driver.findElement(By.name("password"));
		String password = passwordElem.getAttribute("value");
		Assert.assertEquals("", password);
		
		WebElement errorSpan = driver.findElement(By.id("user_address_zipCode_errorMsg"));
		String errorMsg = errorSpan.getText();
		Assert.assertEquals("Could not convert value", errorMsg);
		
		System.out.println("hi there");
		//find the error element?...
		
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
