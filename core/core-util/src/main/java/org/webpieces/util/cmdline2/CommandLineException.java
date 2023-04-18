package org.webpieces.util.cmdline2;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;

public class CommandLineException extends CompletionException {

	private static final long serialVersionUID = 1L;
	private List<Throwable> errors;
	private final Map<String, List<UsageHelp>> cmdLineHelp;
	private final Map<String, List<UsageHelp>> envVarHelp;

	public CommandLineException(String message, List<Throwable> errors, Map<String, List<UsageHelp>> cmdLineHelp, Map<String, List<UsageHelp>> envVarHelp) {
		super(message);
		this.errors = errors;
		this.cmdLineHelp = cmdLineHelp;
		this.envVarHelp = envVarHelp;
	}

	public List<Throwable> getErrors() {
		return errors;
	}

	public Map<String, List<UsageHelp>> getCmdLineHelp() {
		return cmdLineHelp;
	}

	public Map<String, List<UsageHelp>> getEnvVarHelp() {
		return envVarHelp;
	}
}
