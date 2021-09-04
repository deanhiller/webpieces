package org.webpieces.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class MockSuperclass {

	protected Map<MethodEnum, List<ValueToReturn>> returnValues = new HashMap<>();
	protected Map<MethodEnum, List<ParametersPassedIn>> calledMethods = new HashMap<>();
	protected Map<MethodEnum, ValueToReturn> defaultReturnValues = new HashMap<>();
	
	protected void addValueToReturn(MethodEnum method, Object toReturn) {
		ValueToReturn valueToReturn = new ValueToReturn(() -> toReturn);
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
	
	protected void addExceptionToThrow(MethodEnum method, Supplier<?> throwableSupplier) {
		ValueToReturn valueToReturn = new ValueToReturn(throwableSupplier);
		addValueToReturn(method, valueToReturn);
	}
	
	protected Object calledMethod(MethodEnum method, Object ... args) {
		addCalledMethod(method, args);
		
		List<ValueToReturn> list = returnValues.get(method);
		ValueToReturn toReturn;
		if(list == null || list.size() == 0) {
			toReturn = defaultReturnValues.get(method);
			if(toReturn == null)
				throw new IllegalStateException("test did not add enough return vales(or did not set default return value) for method="+method);
		} else {
			toReturn = list.remove(0);			
		}
		
		return toReturn.returnOrThrowValue();
	}
	
	protected void calledVoidMethod(MethodEnum method, Object ... args) {
		addCalledMethod(method, args);
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
		 return getCalledMethodList(method).stream();
	}
	
	protected List<ParametersPassedIn> getCalledMethodList(MethodEnum method) {
		List<ParametersPassedIn> params = calledMethods.get(method);
		if(params == null) {
			return new ArrayList<>();
		}
		
		List<ParametersPassedIn> copy = new ArrayList<>();
		copy.addAll(params);
		params.clear();
		return copy;		
	}
	
	public void clear() {
		calledMethods = new HashMap<>();
		returnValues = new HashMap<>();
	}

	protected void setDefaultReturnValue(MethodEnum method, Object retVal) {
		defaultReturnValues.put(method, new ValueToReturn(() -> retVal));
	}
}
