package org.webpieces.util.cmdline;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.cmdline2.CommandLineException;
import org.webpieces.util.cmdline2.CommandLineParser;


public class CmdLineParser2Test {

	private FakeClient fakeClient;

	@Before
	public void setup() {
		fakeClient = new FakeClient();
		
	}
	
	@Test
	public void testBasicPluginScrewupReadEarly() {
		String[] args = new String[] {"-key1=something", "-key2=something2"};

		fakeClient.fakeMain(args);
	
		try {
			fakeClient.readKey1();
			Assert.fail("Should have thrown.  this is not allowed so we can discover ALL arguments");
		} catch(IllegalStateException e) {
			Assert.assertEquals("Bug in that you are consuming this too early before we are done creating all arguments", e.getMessage());
		}
	}
	
	@Test
	public void testBasicPass() {
		String[] args = new String[] {"-key1=localhost:5", "-key2=something2", "-key5=asf", };

		fakeClient.fakeMain(args);
		
		fakeClient.checkArgs();
		
		InetSocketAddress address = fakeClient.readKey1();
		int value4a = fakeClient.readKey4a();
		Assert.assertEquals("localhost", address.getHostName());
		Assert.assertEquals(5, address.getPort());
		
		Assert.assertEquals(456, value4a);
	}

	@Test
	public void testDefaultNullObjectAfterConversionAllowed() {
		String[] args = new String[] {"-key2=something2", "-key5=asf" };

		fakeClient.fakeMain(args);
		
		fakeClient.checkArgs();
		
		InetSocketAddress address = fakeClient.readKey7();
		Assert.assertEquals(null, address);
	}
	
	@Test
	public void testNullValueOnCmdLineAllowed() {
		String[] args = new String[] {"-key7=", "-key2=something2", "-key5=asf" };

		fakeClient.fakeMain(args);
		
		fakeClient.checkArgs();
		
		InetSocketAddress address = fakeClient.readKey7();
		Assert.assertEquals(null, address);
	}
	
	@Test
	public void testKeyRequiresValueButNoValueExistOptionalAndRequired() {
		String[] args = new String[] {"-key1", "-key2", "-key5=asf", };
		
		fakeClient.fakeMain(args);
		
		try {
			fakeClient.checkArgs();
			Assert.fail("Should have thrown telling developer all errors");
		} catch(CommandLineException e) {
			List<Throwable> errors = e.getErrors();
			Assert.assertEquals("key=key1 was supplied with no value.  A value is required OR remove the key(it's optional)", errors.get(0).getMessage());
			Assert.assertEquals("key=key2 was supplied with no value.  A value is required", errors.get(1).getMessage());
		}
	}
	
	@Test
	public void testOptionalConsumedTwiceDifferentDefaultValueThrowsException() {
		String[] args = new String[] {"-key1=5"};

		Arguments parse = new CommandLineParser().parse(args);

		//different default values not allowed.  both must default to same thing
		//this is a weird case
		parse.createOptionalArg("key1", "123", "key1 help", (s) -> convertInt(s));
		parse.createOptionalArg("key1", "555", "help for different use of same key to give info on his need", (s) -> convertInt(s));
		
		try {
			parse.checkConsumedCorrectly();
			Assert.fail("Should fail since defaults are different");
		} catch(CommandLineException e) {
			Assert.assertEquals("Errors converting command line arguments:\n" + 
					"(Call CommandLineException.getErrors to get the stack trace of each failure)\n" + 
					"java.lang.IllegalStateException: Bug, two people consuming key -key1 but both provide different defaults.  default1=123 default2=555\n" + 
					"\n" + 
					"Dynamically generated help(depends on which plugins you pull in):\n" + 
					"	-key1 following usages:\n" + 
					"		(optional, default: 123)key1 help\n" + 
					"				Value Parsed:5 foundKey:true foundValue:true\n" + 
					"		(optional, default: 555)help for different use of same key to give info on his need\n" + 
					"				Value Parsed:5 foundKey:true foundValue:true\n" + 
					"", e.getMessage());
			List<Throwable> errors = e.getErrors();
			Assert.assertEquals(1, errors.size());
			Assert.assertEquals("Bug, two people consuming key -key1 but both provide different defaults.  default1=123 default2=555", errors.get(0).getMessage());
		}
	}
	
	@Test
	public void testExtraArgumentFails() {
		String[] args = new String[] {"-key1=5", "-key2"};

		Arguments parse = new CommandLineParser().parse(args);

		//different default values not allowed.  both must default to same thing
		//this is a weird case
		parse.createOptionalArg("key1", "123", "key1 help", (s) -> convertInt(s));
		
		try {
			parse.checkConsumedCorrectly();
			Assert.fail("Should fail since someone says key1 is optional and another says it's required");
		} catch(CommandLineException e) {
			List<Throwable> errors = e.getErrors();
			Assert.assertEquals(1, errors.size());
			Assert.assertEquals("Key=key2 was not defined anywhere in the program", errors.get(0).getMessage());
		}
	}
	
	@Test
	public void testKeyIsOptionalAndRequiredNotPassedInShouldFail() {
		String[] args = new String[] { };

		Arguments parse = new CommandLineParser().parse(args);

		//different default values not allowed.  both must default to same thing
		//this is a weird case
		parse.createOptionalArg("key1", "123", "key1 help", (s) -> convertInt(s));
		parse.createRequiredArg("key1", "key1 help from different plugin, and reasons are different", (s) -> convertInt(s));
		
		try {
			parse.checkConsumedCorrectly();
		} catch(CommandLineException e) {
			Assert.assertEquals("Errors converting command line arguments:\n" + 
					"(Call CommandLineException.getErrors to get the stack trace of each failure)\n" + 
					"java.lang.IllegalArgumentException: Argument -key1 is required but was not supplied.  help='key1 help from different plugin, and reasons are different'\n" + 
					"\n" + 
					"Dynamically generated help(depends on which plugins you pull in):\n" + 
					"	-key1 following usages:\n" + 
					"		(optional, default: 123)key1 help\n" + 
					"				Value Parsed:null foundKey:false foundValue:false\n" + 
					"		key1 help from different plugin, and reasons are different\n" + 
					"				Value Parsed:null foundKey:false foundValue:false\n", e.getMessage());
		}
		
		
	}
	
	@Test
	public void testKeyIsOptionalAndRequiredPassedInShouldSucceed() {
		String[] args = new String[] {"-key1=5"};

		Arguments parse = new CommandLineParser().parse(args);

		//different default values not allowed.  both must default to same thing
		//this is a weird case
		Supplier<Integer> val1 = parse.createOptionalArg("key1", "123", "key1 help", (s) -> convertInt(s));
		Supplier<Integer> val2 = parse.createRequiredArg("key1", "key1 help", (s) -> convertInt(s));
		
		parse.checkConsumedCorrectly();
		
		Assert.assertEquals(Integer.valueOf(5), val1.get());
		Assert.assertEquals(Integer.valueOf(5), val2.get());
	}

	@Test
	public void testKeyOrEnvVarIsRequiredNotPassedInShouldFail() {
		String[] args = new String[] { };

		Arguments parse = new CommandLineParser().parse(args);

		parse.createRequiredArgOrEnvVar("key1", "TEST_EMPTY", "key1 help", (s) -> convertInt(s));

		try {
			parse.checkConsumedCorrectly();
			Assert.fail("Should fail since someone says key1 or TEST_EMPTY is required but it is not passed");
		} catch(CommandLineException e) {
			List<Throwable> errors = e.getErrors();
			Assert.assertEquals(1, errors.size());
			Assert.assertEquals("Argument -key1 or env var TEST_EMPTY is required but was not supplied.  help='key1 help'", errors.get(0).getMessage());
		}

	}

	@Test
	public void testKeyOrEnvVarIsRequiredPassedInShouldSucceed() {
		String[] args = new String[] {"-key2=5"};
		addEnv("TEST_NOT_EMPTY", "123");

		Arguments parse = new CommandLineParser().parse(args);

		//different default values not allowed.  both must default to same thing
		//this is a weird case
		Supplier<Integer> val1 = parse.createRequiredArgOrEnvVar("key1", "TEST_NOT_EMPTY", "key1 help", (s) -> convertInt(s));
		Supplier<Integer> val2 = parse.createRequiredArgOrEnvVar("key2", "TEST_EMPTY", "key1 help", (s) -> convertInt(s));

		parse.checkConsumedCorrectly();

		Assert.assertEquals(Integer.valueOf(123), val1.get());
		Assert.assertEquals(Integer.valueOf(5), val2.get());
	}
	
	@Test
	public void testFailWithNoDash() {
		String[] args = new String[] {"-key1=3", "key2=6"};

		Arguments parse = new CommandLineParser().parse(args);

		//different default values not allowed.  both must default to same thing
		//this is a weird case
		parse.createOptionalArg("key1", "123", "key1 help", (s) -> convertInt(s));
		parse.createRequiredArg("key2", "key2 help", (s) -> convertInt(s));
		
		try {
			parse.checkConsumedCorrectly();
			Assert.fail("Should fail since there is no -");
		} catch(CommandLineException e) {
			List<Throwable> errors = e.getErrors();
			Assert.assertEquals(2, errors.size());
			Assert.assertEquals("Argument 'key2=6' has a key that does not start with - which is required", errors.get(0).getMessage());
			Assert.assertEquals("Argument -key2 is required but was not supplied.  help='key2 help'", errors.get(1).getMessage());
		}
	}
	
	@Test
	public void testDeveloperHasInvalidDefaultValueAndRequiredMissing() {
		String[] args = new String[] {"-key1=something"};

		Arguments parse = new CommandLineParser().parse(args);

		parse.createOptionalArg("key1", "invalid", "key1 help", (s) -> convertInt(s));
		parse.createRequiredArg("key3", "Some key3", (s) -> s);

		try {
			parse.checkConsumedCorrectly();
			Assert.fail("Should have thrown telling developer all errors");
		} catch(CommandLineException e) {
			List<Throwable> errors = e.getErrors();
			Assert.assertEquals(2, errors.size());
			Assert.assertEquals("Bug, The defaultValue conversion test failed.  key=key1 value=invalid", errors.get(0).getMessage());
			Assert.assertEquals("Argument -key3 is required but was not supplied.  help='Some key3'", errors.get(1).getMessage());
		}
	}
	
	@Test
	public void testConsumeExistsKey3TypeOfKeyIsTrue() {
		String[] args = new String[] {"-key3", "-key5=asdf", "-key2=wqeee"};

		fakeClient.fakeMain(args);
		
		fakeClient.checkArgs();
		
		boolean value = fakeClient.isKey3();
		Assert.assertTrue(value);
	}

	@Test
	public void testConsumeKey3ExistsTypeOfKeyIsFalse() {
		String[] args = new String[] {"-key5=asdf", "-key2=wqeee"};

		fakeClient.fakeMain(args);
		
		fakeClient.checkArgs();
		
		boolean value = fakeClient.isKey3();
		Assert.assertFalse(value);
	}
	
	private Integer convertInt(String s) {
		return Integer.parseInt(s);
	}

	@SuppressWarnings("unchecked")
	private void addEnv(String name, String val) {
		Map<String, String> env = System.getenv();
		Field field = null;
		try {
			field = env.getClass().getDeclaredField("m");
			field.setAccessible(true);
			((Map<String, String>) field.get(env)).put(name, val);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
