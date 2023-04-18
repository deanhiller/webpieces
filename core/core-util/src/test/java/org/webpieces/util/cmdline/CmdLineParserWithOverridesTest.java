package org.webpieces.util.cmdline;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.util.cmdline2.ArgumentsCheck;
import org.webpieces.util.cmdline2.CommandLineParser;
import org.webpieces.util.cmdline2.AllowDefaultingRequiredVars;
import org.webpieces.util.cmdline2.FetchValue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


public class CmdLineParserWithOverridesTest {


	private MockEnv mockEnv;

	private CommandLineParser cmdLineParser;

	@Before
	public void setup() {
		mockEnv = new MockEnv();
		FetchValue fetchValue = new AllowDefaultingRequiredVars();
		cmdLineParser = new CommandLineParser(mockEnv, fetchValue);
	}

	private Integer convertInt(String s) {
		return Integer.parseInt(s);
	}

	@Test
	public void testRequiredEnvVarsFailIfNotDefined() {
		mockEnv.setEnvironment(new HashMap<>());

		ArgumentsCheck parse = cmdLineParser.parse();

		Supplier<String> requiredEnvVar = parse.createRequiredEnvVar("REQUIRED_ENV_VAR", null, "help msg");

		try {
			parse.checkConsumedCorrectly();
			Assert.fail("Should fail with requires REQUIRED_ENV_VAR environment variable");
		} catch (Exception e) {
		}
	}

	@Test
	public void testRequiredEnvVarsFilledInWithDefault() {
		mockEnv.setEnvironment(new HashMap<>());

		ArgumentsCheck parse = cmdLineParser.parse();

		Supplier<Integer> requiredEnvVar = parse.createRequiredEnvVar("REQUIRED_ENV_VAR", 99, "help msg", (s) -> convertInt(s));

		parse.checkConsumedCorrectly();

		Assert.assertEquals(Integer.valueOf(99), requiredEnvVar.get());
	}

	/**
	 * We still need to use the argument defined in tests over the default since tests need to
	 * vary the environment value for testing.  Default is only there for tests not defining it.
	 */
	@Test
	public void testRequiredEnvVarDoesNotUseDefaultAndUsesRealArg() {
		mockEnv.setEnvironment(Map.of("REQUIRED_ENV_VAR", "72"));

		ArgumentsCheck parse = cmdLineParser.parse();

		Supplier<Integer> requiredEnvVar = parse.createRequiredEnvVar("REQUIRED_ENV_VAR", 101, "help msg", (s) -> convertInt(s));

		parse.checkConsumedCorrectly();

		Assert.assertEquals(Integer.valueOf(72), requiredEnvVar.get());
	}

	@Test
	public void testRequiredCmdLineArgsFailIfNotDefined() {
		ArgumentsCheck parse = cmdLineParser.parse();

		Supplier<String> arg = parse.createRequiredArg("requiredArg", null, "help msg");

		try {
			parse.checkConsumedCorrectly();
			Assert.fail("Should fail with requires REQUIRED_ENV_VAR environment variable");
		} catch (Exception e) {
		}
	}

	@Test
	public void testRequiredCmdLineArgsFilledInWithDefault() {
		ArgumentsCheck parse = cmdLineParser.parse();

		Supplier<String> arg = parse.createRequiredArg("requiredArg", "def", "help msg");

		parse.checkConsumedCorrectly();

		Assert.assertEquals("def", arg.get());
	}

	@Test
	public void testRequiredCmdLineArgDoesNotUseDefaultAndUsesRealArg() {
		ArgumentsCheck parse = cmdLineParser.parse("-requiredArg=89");

		Supplier<Integer> arg = parse.createRequiredArg("requiredArg", 102, "help msg", (s) -> convertInt(s));

		parse.checkConsumedCorrectly();

		Assert.assertEquals(Integer.valueOf(89), arg.get());
	}
}
