package org.webpieces.util.cmdline;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;


public class CmdLineParserTest {

	@Test
	public void testCmdLineParsing() {
		String[] args = new String[] {"-key1=something", "-key2=something2"};
	
		Map<String, String> map = new CommandLineParser().parse(args);
		Assert.assertEquals("something", map.get("key1"));
		Assert.assertEquals("something2", map.get("key2"));
	}
	
	@Test
	public void testFailWithNoDash() {
		String[] args = new String[] {"-key1=something", "key2=something2"};

		try {
			new CommandLineParser().parse(args);
			Assert.fail("should have failed and didn't");
		} catch(IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains(" has a key that does not start with - "));
		}

	}
	
	@Test
	public void testFailWithNoEquals() {
		String[] args = new String[] {"-key1=something", "-key2"};

		try {
			new CommandLineParser().parse(args);
			Assert.fail("should have failed and didn't");
		} catch(IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains(" has bad syntax.  It either has no ="));
		}

	}
	
}
