package org.webpieces.mock;

import java.util.Arrays;
import java.util.List;

public class ParametersPassedIn {

	private Object[] args;

	public ParametersPassedIn(Object[] args) {
		this.args = args;
	}

	public Object[] getArgs() {
		return args;
	}

	@Override
	public String toString() {
		List<Object> list = Arrays.asList(args);
		return "{arguments="+list+"}";
	}
	
}
