package org.webpieces.util.filters;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ExceptionUtil {
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
	    	return CompletableFuture.<T>failedFuture(e.getCause());
	    } catch (Exception ex) {
	        return CompletableFuture.failedFuture(ex);
	    }
	}
	
	public static <T> CompletableFuture<T> wrap(Callable<CompletableFuture<T>> callable, Function<Throwable, Throwable> wrapException) {
	    try {
	        return callable.call();
	    } catch (Exception ex) {
	        return CompletableFuture.failedFuture(wrapException.apply(ex));
	    }
	}
}
