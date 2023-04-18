package org.webpieces.util.cmdline;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.util.cmdline2.*;
import org.webpieces.util.cmdline2.CommandLineParser;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


public class CmdLineParserEnvVarsTest {


	private MockEnv mockEnv;

	private CommandLineParser cmdLineParser;

	@Before
	public void setup() {
		mockEnv = new MockEnv();
		FetchValue fetchValue = new FetchValue();
		cmdLineParser = new CommandLineParser(mockEnv, fetchValue);
	}

	private Integer convertInt(String s) {
		return Integer.parseInt(s);
	}

	@Test
	public void testOptionalEnvVarsReturnsNullIfNotDefined() {
		mockEnv.setEnvironment(new HashMap<>());

		ArgumentsCheck parse = cmdLineParser.parse();

		Supplier<String> optionalEnvVar = parse.createOptionalEnvVar("OPTIONAL_ENV_VAR", null, "help msg");

		parse.checkConsumedCorrectly();

		Assert.assertNull(optionalEnvVar.get());
	}

	@Test
	public void testOptionalEnvVarsReturnsDefaultIfNotDefined() {
		mockEnv.setEnvironment(new HashMap<>());

		ArgumentsCheck parse = cmdLineParser.parse();

		Supplier<String> optionalEnvVar = parse.createOptionalEnvVar("OPTIONAL_ENV_VAR", "myVal", "help msg");

		parse.checkConsumedCorrectly();

		Assert.assertEquals("myVal", optionalEnvVar.get());
	}

	@Test
	public void testOptionalEnvVarsReturnsValue() {
		mockEnv.setEnvironment(Map.of("OPTIONAL_ENV_VAR", "56"));

		ArgumentsCheck parse = cmdLineParser.parse();

		Supplier<Integer> optionalEnvVar = parse.createOptionalEnvVar("OPTIONAL_ENV_VAR", "90", "help msg", (s)-> convertInt(s));

		parse.checkConsumedCorrectly();

		Assert.assertEquals(Integer.valueOf(56), optionalEnvVar.get());
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
	public void testRequiredEnvVarsPassthrough() {
		mockEnv.setEnvironment(Map.of("REQUIRED_ENV_VAR", "72"));

		ArgumentsCheck parse = cmdLineParser.parse();

		Supplier<Integer> requiredEnvVar = parse.createRequiredEnvVar("REQUIRED_ENV_VAR", null, "help msg", (s) -> convertInt(s));

		parse.checkConsumedCorrectly();

		Assert.assertEquals(Integer.valueOf(72), requiredEnvVar.get());
	}



}
