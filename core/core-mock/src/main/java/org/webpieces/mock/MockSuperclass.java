package org.webpieces.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class MockSuperclass {

	private Map<MethodEnum, List<ValueToReturn>> returnValues = new HashMap<>();
	private Map<MethodEnum, List<ParametersPassedIn>> calledMethods = new HashMap<>();
	
	protected void addValueToReturn(MethodEnum method, Object toReturn) {
		ValueToReturn valueToReturn = new ValueToReturn(toReturn);
		addValueToReturn(method, valueToReturn);
	}

	private void addValueToReturn(MethodEnum method, ValueToReturn valueToReturn) {
		List<ValueToReturn> currentValues = returnValues.get(method);
		if(currentValues == null) {
			currentValues = new ArrayList<>();
			returnValues.put(method, currentValues);
		}
		currentValues.add(valueToReturn);
	}
	
	protected void addExceptionToThrow(MethodEnum method, RuntimeException exc) {
		ValueToReturn valueToReturn = new ValueToReturn(exc);
		addValueToReturn(method, valueToReturn);
	}
	
	protected Object calledMethod(MethodEnum method, Object ... args) {
		addCalledMethod(method, args);
		
		List<ValueToReturn> list = returnValues.get(method);
		if(list == null || list.size() == 0)
			return null;
		
		ValueToReturn toReturn = list.remove(0);
		return toReturn.returnOrThrowValue();
	}

	private void addCalledMethod(MethodEnum method, Object[] args) {
		List<ParametersPassedIn> list = calledMethods.get(method);
		if(list == null) {
			list = new ArrayList<>();
			calledMethods.put(method, list);
		}
		list.add(new ParametersPassedIn(args));
	}

	protected Stream<ParametersPassedIn> getCalledMethods(MethodEnum method) {
		List<ParametersPassedIn> params = calledMethods.get(method);
		if(params == null) {
			params = new ArrayList<>();
		}
		return params.stream();
	}
}
