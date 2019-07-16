package org.webpieces.util.cmdline2;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class SupplierImpl<T> implements Supplier<T> {

	private final T param;
	private final AtomicBoolean isAllArgsConsumed;

	public SupplierImpl(T param, boolean isOptional, AtomicBoolean isAllArgsConsumed) {
		this.param = param;
		this.isAllArgsConsumed = isAllArgsConsumed;
	}

	//required AND was not on command line so if we get to consumption, the client
	//is using this library wrong as we should fail long before the consuming of
	//arguments phase.  (This is a two phase system...load all Suppliers but
	//don't read from them)
	public SupplierImpl(AtomicBoolean isAllArgsConsumed) {
		param = null;
		this.isAllArgsConsumed = isAllArgsConsumed;
	}

	@Override
	public T get() {
		if(!isAllArgsConsumed.get())
			throw new IllegalStateException("Bug in that you are consuming this too early before we are done creating all arguments");
		return param;
	}

}
