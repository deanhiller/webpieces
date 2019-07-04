package org.webpieces.util.cmdline2;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;

public class CommandLineException extends CompletionException {

	private static final long serialVersionUID = 1L;
	private List<Throwable> errors;
	private Map<String, List<UsageHelp>> dynamicallyGeneratedHelp;

	public CommandLineException(String message, List<Throwable> errors, Map<String, List<UsageHelp>> dynamicallyGeneratedHelp) {
		super(message);
		this.errors = errors;
		this.dynamicallyGeneratedHelp = dynamicallyGeneratedHelp;
	}

	public List<Throwable> getErrors() {
		return errors;
	}

	public Map<String, List<UsageHelp>> getDynamicallyGeneratedHelp() {
		return dynamicallyGeneratedHelp;
	}
	
}
