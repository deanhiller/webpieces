package org.webpieces.mock;

public class ValueToReturn {

	private Object toReturn;
	private RuntimeException exception;

	public ValueToReturn(Object toReturn) {
		this.toReturn = toReturn;
	}
	
	public ValueToReturn(RuntimeException exc) {
		this.exception = exc;
	}

	public Object returnOrThrowValue() {
		if(exception != null) {
			//This is so it is a brand new stack trace...
			Class<? extends RuntimeException> clazz = exception.getClass();
			RuntimeException newInstance = newInstance(clazz);
			newInstance.initCause(exception);
			throw newInstance;
		}
		
		return toReturn;
	}

	private RuntimeException newInstance(Class<? extends RuntimeException> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
