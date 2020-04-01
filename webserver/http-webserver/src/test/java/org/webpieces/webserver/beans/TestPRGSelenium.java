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
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.webserver.PrivateWebserverForTest;
import org.webpieces.webserver.test.Asserts;
import org.webpieces.webserver.test.OverridesForTestRealServer;

import com.google.inject.Binder;
import com.google.inject.Module;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

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
		
		VirtualFileClasspath metaFile = new VirtualFileClasspath("beansMeta.txt", PrivateWebserverForTest.class.getClassLoader());
		PrivateWebserverForTest webserver = new PrivateWebserverForTest(new OverridesForTestRealServer(new SimpleMeterRegistry()), new AppOverridesModule(), true, metaFile);
		webserver.start();
		port = webserver.getUnderlyingHttpChannel().getLocalAddress().getPort();
	}
	

	
	//You must have firefox installed to run this test...
	@Test
	public void testBasicForm() throws ClassNotFoundException {
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
		
		String pageSource2 = driver.getPageSource();
		Assert.assertFalse("pageSource="+pageSource2, pageSource2.contains("null"));
		
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
		
		String pageSource3 = driver.getPageSource();
		Assert.assertFalse("pageSource="+pageSource3, pageSource3.contains("null"));
		//ensure error message is there
		Assert.assertTrue("pageSource="+pageSource3, pageSource3.contains("Msg: Invalid values below"));		
	}
	
	//You must have firefox installed to run this test...
	@Test
	public void testArrayFieldInForm() throws ClassNotFoundException {
		driver.get("http://localhost:"+port+"/arrayForm");
		
		String pageSource = driver.getPageSource();
		Assert.assertTrue("pageSource="+pageSource, pageSource.contains("Add User"));

		WebElement userInput = driver.findElement(By.name("user.accounts[0].addresses[0].street"));
		userInput.sendKeys("Dean Hiller Street");
		
		WebElement zipCodeInput = driver.findElement(By.name("user.accounts[0].addresses[1].street"));
		zipCodeInput.clear();
		zipCodeInput.sendKeys("Street2");		
		
		WebElement submit = driver.findElement(By.id("submit"));
		submit.submit();

		//ensure flash scope worked and what the user typed in is still there
		userInput = driver.findElement(By.name("user.accounts[0].addresses[0].street"));
		String userName = userInput.getAttribute("value");
		Assert.assertEquals("Dean Hiller Street", userName);
		
		zipCodeInput = driver.findElement(By.name("user.accounts[0].addresses[1].street"));
		String password = zipCodeInput.getAttribute("value");
		Assert.assertEquals("Street2", password);

		Assert.assertEquals("http://localhost:"+port+"/arrayForm", driver.getCurrentUrl());

		WebElement errorSpan = driver.findElement(By.id("user_accounts:0:_addresses:0:_street_errorMsg"));
		String errorMsg = errorSpan.getText();
		Assert.assertEquals("This is too ugly a street name", errorMsg);
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
