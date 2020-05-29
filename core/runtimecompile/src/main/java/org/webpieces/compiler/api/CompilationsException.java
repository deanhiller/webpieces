package org.webpieces.compiler.api;

import java.util.List;
import java.util.concurrent.CompletionException;

public class CompilationsException extends CompletionException {

	private static final long serialVersionUID = 1L;
	private List<CompileError> compileErrors;

	public CompilationsException(List<CompileError> compileErrors, String fullMessage) {
		super(fullMessage);
		this.compileErrors = compileErrors;
	}

	public List<CompileError> getCompileErrors() {
		return compileErrors;
	}

	
}
