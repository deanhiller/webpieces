package org.webpieces.util.futures;

import org.webpieces.util.futures.XFuture;

public interface Processor<T> {

	public XFuture<Void> process(T item);
}
