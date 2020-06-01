package org.webpieces.util.futures;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class XFuture<T> extends CompletableFuture<T> {

	private Function<Object, Boolean> cancelFunction;
    
	public XFuture() {}
	
	public XFuture(Function<Object, Boolean> cancelFunction) {
		this.cancelFunction = cancelFunction;
	}
	
    public XFuture<Void> thenAccept(Consumer<? super T> originalFunction) {
		Map<String, Object> state = FutureLocal.fetchState();
    	
    	Consumer<? super T> c2 = (s) -> {
			Map<String, Object> prevState = FutureLocal.fetchState();
			try {
				FutureLocal.restoreState(state);
				
				originalFunction.accept(s);
				
			} finally {
				FutureLocal.restoreState(prevState);
			}
    	};

    	return (XFuture<Void>) super.thenAccept(c2);
    }

	public T join() {
		return super.join();
	}

    @SuppressWarnings("unchecked")
	public <U> XFuture<U> thenApplyAsync(
            Function<? super T,? extends U> fn, Executor executor) {
		Map<String, Object> state = FutureLocal.fetchState();
		MyFunction f = new MyFunction(state, fn);		
		
		return (XFuture<U>) super.thenApplyAsync(f, executor);    	
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

    @SuppressWarnings("unchecked")
	public XFuture<T> exceptionally(
            Function<Throwable, ? extends T> fn) {
		Map<String, Object> state = FutureLocal.fetchState();
		MyFunction f = new MyFunction(state, fn);

		return (XFuture<T>) super.exceptionally(f);
    }
	
    @Override
	public <U> XFuture<U> newIncompleteFuture() {
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

    
    @SuppressWarnings("unchecked")
	@Override
	public <U> XFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
		return (XFuture<U>) super.handle(fn);
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

	/**
	 * A special cancel that regardless of futures completing or not, this one sends cancels through the
	 * chain of futures unless the chain is broken by constructing a middle man future with no
	 * cancelFunction.  This can be used to cancel streaming apis after they have started
	 */
	public boolean cancelChain(Object reason) {
		if(cancelFunction != null)
			return cancelFunction.apply(reason);
	
		return false;
	}
	
	public static XFuture<Void> allOf(XFuture<?> ... cfs) {
    	CompletableFuture<Void> allOf = CompletableFuture.allOf(cfs);
    	
    	Function<Object, Boolean> cancelFunc = (s) -> {
    		boolean allCancelled = true;
    		for(XFuture<?> f: cfs) {
    			boolean wasCancelled = f.cancelChain(s);
    			if(!wasCancelled)
    				allCancelled = false;
    		}
    		return allCancelled;
    		
    	};
    	

    	return convert(allOf, cancelFunc);
    }

	public static <T> XFuture<T> convert(CompletableFuture<T> future1) {
		return convert(future1, null);
	}
	
	public static <T> XFuture<T> convert(CompletableFuture<T> future1, Function<Object, Boolean> cancelFunc) {
		
    	
    	XFuture<T> xFuture = new XFuture<T>(cancelFunc);
    	future1.handle( (resp, t) -> {
    		if(t != null)
    			xFuture.completeExceptionally(t);
    		else
    			xFuture.complete(resp);
    		
    		return null;
    	});
		
		return xFuture;
	}
}
