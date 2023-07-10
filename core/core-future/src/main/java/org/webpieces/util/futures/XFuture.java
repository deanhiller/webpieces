package org.webpieces.util.futures;

import org.webpieces.util.context.Context;

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
		MyConsumer<? super T> consumer = new MyConsumer<>(originalFunction);
    	return (XFuture<Void>) super.thenAccept(consumer);
    }

	public T join() {
		return super.join();
	}

    @SuppressWarnings("unchecked")
	public <U> XFuture<U> thenApplyAsync(
            Function<? super T,? extends U> fn, Executor executor) {
		MyFunction<? super T,? extends U> f = new MyFunction<>(fn);
		return (XFuture<U>) super.thenApplyAsync(f, executor);
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public <U> XFuture<U> thenApply(Function<? super T, ? extends U> fn) {
		MyFunction<? super T, ? extends U> f = new MyFunction<>(fn);
		return (XFuture<U>) super.thenApply(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> XFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
		MyFunction<? super T, ? extends CompletionStage<U>> f = new MyFunction<>(fn);
		return (XFuture<U>) super.thenCompose(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> XFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
		MyBiFunction<? super T, Throwable, ? extends U> f = new MyBiFunction<>(fn);
		return (XFuture<U>) super.handle(f);
	}

    @SuppressWarnings("unchecked")
	public XFuture<T> exceptionally(
            Function<Throwable, ? extends T> fn) {
		MyFunction<Throwable, ? extends T> f = new MyFunction<>(fn);
		return (XFuture<T>) super.exceptionally(f);
    }
	
    //@Override
	public <U> XFuture<U> newIncompleteFuture() {
		return new XFuture<U>(cancelFunction);
	}

	private class MyConsumer<T> implements Consumer<T> {

		private Map<String, Object> state;
		private Consumer<T> originalFunction;

		public MyConsumer(Consumer<T> originalFunction) {
			//if you do not copy, the MDC.remove in the finally blocks run LONG before using this so
			//the state becomes corrupted.  you must copy it
			this.state = Context.copyContext();
			this.originalFunction = originalFunction;
		}

		@Override
		public void accept(T t) {

			Map<String, Object> prevState = Context.copyContext();
			try {
				Context.setContext(state);

				originalFunction.accept(t);

			} finally {
				Context.setContext(prevState);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private class MyFunction<REQ,  RESP> implements Function<REQ, RESP> {

		private Map<String, Object> state;
		private Function<REQ, RESP> originalFunction;

		public MyFunction(Function<REQ, RESP> originalFunction) {
			//if you do not copy, the MDC.remove in the finally blocks run LONG before using this so
			//the state becomes corrupted.  you must copy it
			this.state = Context.copyContext();
			this.originalFunction = originalFunction;
		}

		@Override
		public RESP apply(REQ t) {

			Map<String, Object> prevState = Context.copyContext();
			try {
				Context.setContext(state);
				
				return originalFunction.apply(t);
				
			} finally {
				Context.setContext(prevState);
			}
		}
	}

	private class MyBiFunction<T, U, R> implements BiFunction<T, U, R> {

		private Map<String, Object> state;
		private BiFunction<T, U, R> originalFunction;

		public MyBiFunction(BiFunction<T, U, R> originalFunction) {
			this.state = Context.copyContext();
			this.originalFunction = originalFunction;
		}

		@Override
		public R apply(T o, U o2) {
			Map<String, Object> prevState = Context.copyContext();
			try {
				Context.setContext(state);

				return originalFunction.apply(o, o2);

			} finally {
				Context.setContext(prevState);
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

	public static <T> XFuture<T> convert(XFuture<T> future1) {
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
