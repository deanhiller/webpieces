package org.webpieces.util.futures;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class XFuture<T> extends CompletableFuture<T> {

	private Function<Object, Boolean> cancelFunction;

    static final AltResult NIL = new AltResult(null);

    static final class AltResult { // See above
        final Throwable ex;        // null only for NIL
        AltResult(Throwable x) { this.ex = x; }
    }
    
	public XFuture() {}
	
	public XFuture(Function<Object, Boolean> cancelFunction) {
		this.cancelFunction = cancelFunction;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <U> XFuture<U> thenApply(Function<? super T, ? extends U> fn) {
		Map<String, Object> state = FutureLocal.fetchState();
		MyFunction f = new MyFunction(state, fn);		
		
		return (XFuture<U>) super.thenApply(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> XFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
		Map<String, Object> state = FutureLocal.fetchState();
		MyFunction f = new MyFunction(state, fn);
		
		return (XFuture<U>) super.thenCompose(f);
	}

	
	@Override
	public <U> CompletableFuture<U> newIncompleteFuture() {
		return new XFuture<U>(cancelFunction);
	}

	@SuppressWarnings("rawtypes")
	private class MyFunction implements Function {

		private Map<String, Object> state;
		private Function originalFunction;

		public MyFunction(Map<String, Object> state, Function originalFunction) {
			this.state = state;
			this.originalFunction = originalFunction;
			
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object apply(Object t) {

			Map<String, Object> prevState = FutureLocal.fetchState();
			try {
				FutureLocal.restoreState(state);
				
				return originalFunction.apply(t);
				
			} finally {
				FutureLocal.restoreState(prevState);
			}
			
			
		}
		
	}

    public static <U> XFuture<U> completedFuture(U value, Function<Object, Boolean> cancelFunction) {
    	XFuture<U> f = new XFuture<U>(cancelFunction);
    	f.complete(value);
    	return f;
    }
    
    public static <U> XFuture<U> completedFuture(U value) {
    	XFuture<U> f = new XFuture<U>();
    	f.complete(value);
    	return f;
    }

    public static <U> XFuture<U> failedFuture(Throwable t, Function<Object, Boolean> cancelFunction) {
    	XFuture<U> f = new XFuture<U>(cancelFunction);
    	f.completeExceptionally(t);
    	return f;    	
    }
    
    public static <U> XFuture<U> failedFuture(Throwable t) {
    	XFuture<U> f = new XFuture<U>();
    	f.completeExceptionally(t);
    	return f;    	
    }
    
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return super.cancel(mayInterruptIfRunning);
	}

	public boolean cancel(Object reason) {
		if(cancelFunction != null)
			return cancelFunction.apply(reason);
	
		return false;
	}
	
}
