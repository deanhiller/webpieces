package org.webpieces.util.futures;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ExceptionUtil {
	

	/**
	 * MUST convert all synchronous futures into Future.exception(t) so we can handle them
	 * all the same 
	 */
	public static <T> CompletableFuture<T> wrapException(
			Callable<CompletableFuture<T>> function, 
			Function<Throwable, Throwable> chainedException
	) {
		//first, must convert any synchronous exceptions to Futures...
		CompletableFuture<T> future = new CompletableFuture<T>();
		try {
			future = function.call();
			
	    	//specifically, it's important that NotFoundException is unwrapped so above layers can catch
			//that instead of this damn InvocationTargetException
		} catch (InvocationTargetException e) {
			future.completeExceptionally(e.getCause());
		} catch(Throwable t) {
			future.completeExceptionally(t);
		}
		
		return wrapExceptionWithConversion(future, chainedException);
	}

	public static <T> CompletableFuture<T> finallyBlock(
			Callable<CompletableFuture<T>> function,
			Runnable finallyCode
	) {
		//convert sync exceptions into async exceptions so we can re-use same exception handling logic.. 
		CompletableFuture<T> future = wrap(function);
		
		//Now, handle ANY (sync or aysnc) exceptions the same by running a finally block......
		CompletableFuture<T> local = future.handle((r, t) -> {
			//RUN the finally code BUT try....catch it
			try {
				finallyCode.run();
			} catch (Exception e) {
				CompletableFuture<T> failedFuture = new CompletableFuture<>();
				if(t != null) {				
					failedFuture.completeExceptionally(t);
					t.addSuppressed(e);
					return failedFuture;
				} else {
					failedFuture.completeExceptionally(e);
					return failedFuture;
					
				}
			}
			
			if(t != null) {
				CompletableFuture<T> failedFuture = new CompletableFuture<>();
				failedFuture.completeExceptionally(t);
				return failedFuture;
			}
			
			CompletableFuture<T> res = CompletableFuture.<T>completedFuture(r);
			return res;
		}).thenCompose(Function.identity());
	
		return local;
	}
	
	private static <T> CompletableFuture<T> wrapExceptionWithConversion(
			CompletableFuture<T> future, 
			Function<Throwable, Throwable> chainedException
	) {
		CompletableFuture<T> local = future.handle((r, t) -> {
			if(t != null) {
				CompletableFuture<T> failedFuture = new CompletableFuture<>();
				failedFuture.completeExceptionally(chainedException.apply(t));
				return failedFuture;
			}
			
			CompletableFuture<T> res = CompletableFuture.<T>completedFuture(r);
			return res;
		}).thenCompose(Function.identity());
	
		return local;
	}
	
	/**
	 * Copying Twitter filters, we convert all synchronous exceptions to CompletableFuture.failedFuture
	 * as there has never been a need over all twitter's 1000's of servers that exist to distinguish 
	 * between a synchronous exception and an asynchronous one.
	 */
	public static <T> CompletableFuture<T> wrap(Callable<CompletableFuture<T>> callable) {
	    try {
			//DAMN these damn InvocationTargetExceptions that just fucking wrap the original
			//GET rid of checked exceptions....in reality InvocationTargetException == FUCKING ANYTHING!!!
			//This ensures the original thrown exception comes through. 
	    	
	    	//specifically, it's important that NotFoundException is unwrapped so above layers can catch
			//that instead of this damn InvocationTargetException
	        return callable.call();
	    } catch (InvocationTargetException e) {
	    	return failedFuture(e.getCause());
	    } catch (Exception ex) {
	        return failedFuture(ex);
	    }
	}

    //In jdk8, they don't have CompletableFuture.failedFuture yet :(	
    public static <T> CompletableFuture<T> failedFuture(Throwable ex) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(ex);
        return future;
    }

	public static <T> CompletableFuture<T> wrap(Callable<CompletableFuture<T>> callable, Function<Throwable, Throwable> wrapException) {
	    try {
	        return callable.call();
	    } catch (Exception ex) {
	        return failedFuture(wrapException.apply(ex));
	    }
	}

}
