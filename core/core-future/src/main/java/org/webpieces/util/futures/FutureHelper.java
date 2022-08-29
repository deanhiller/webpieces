package org.webpieces.util.futures;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class FutureHelper {

	/**
	 * USAGE:
	 *
	 *	    XFuture<Response> future = futureUtil.tryCatchFinallyBlock(
	 *			() -> handleCompleteRequestImpl(),
	 *		    () -> runOnSuccessOfAboveMethod()
	 *			() -> runOnSuccessOrFailureOfAboveTwoMethods()
	 *	    );
	 */
	public <T> XFuture<T> trySuccessFinallyBlock(
			Callable<XFuture<T>> function,
			Callable<XFuture<T>> successFunction,
			Runnable finallyCode
	) {
		//convert sync exceptions into async exceptions so we can re-use same exception handling logic..
		XFuture<T> future = syncToAsyncException(function);
		XFuture<T> newFuture = future.thenCompose( result -> syncToAsyncException(successFunction));
		
		XFuture<T> lastFuture = finallyBlock(
				() -> newFuture,
				() -> finallyCode.run()
		);

		return lastFuture;
	}

	/**
	 * USAGE: 
	 * 
	 *      //In this example, you might set the MDC and clear it in a finally block like so
	 * 		MDC.put("txId", generate());
	 * 
	 *	    XFuture<Response> future = futureUtil.finallyBlock(
	 *			() -> handleCompleteRequestImpl(), 
	 *			() -> MDC.put("txId", null)
	 *	    );
	 */
	public <T> XFuture<T> finallyBlock(
			Callable<XFuture<T>> function,
			Runnable finallyCode
	) {
		//convert sync exceptions into async exceptions so we can re-use same exception handling logic.. 
		XFuture<T> future = syncToAsyncException(function);
		
		//Now, handle ANY (sync or aysnc) exceptions the same by running a finally block......
		XFuture<T> local = future.handle((r, t) -> {
			//RUN the finally code BUT try....catch it
			try {
				finallyCode.run();
			} catch (Exception e) {
				XFuture<T> failedFuture = new XFuture<>();
				if(t != null) {				
					failedFuture.completeExceptionally(t);
					t.addSuppressed(e);
					return failedFuture;
				} else {
					failedFuture.completeExceptionally(e);
					return failedFuture;
					
				}
			}
			
			//r and t can BOTH be null but t is not null on failure and null on good response
			if(t == null) //response is good so return it
				return XFuture.completedFuture(r);
			
			XFuture<T> failedFuture = new XFuture<>();
			failedFuture.completeExceptionally(t);
			return failedFuture;
		}).thenCompose(Function.identity());
	
		return local;
	}

	/**
	 * USAGE: 
	 * 
	 *      //In this example, you may catch and recover or catch and fail like a normal
	 *      //synchronous catch block EXCEPT this is for both async and sync exceptions 
	 *      //from the function
	 * 
	 *	    XFuture<Response> future = futureUtil.catchBlock(
	 *			() -> handleCompleteRequestImpl(), 
	 *			(t) -> runCatchBlock(t) //can return success if recovering OR failure if not
	 *	    );
	 */
	public <T> XFuture<T> catchBlock(
		Callable<XFuture<T>> function,
		Function<Throwable, XFuture<T>> catchBlock
	) {
		//convert sync exceptions into async exceptions so we can re-use same exception handling logic.. 
		XFuture<T> future = syncToAsyncException(function);
		
		//Now, handle ANY (sync or aysnc) exceptions the same by running a finally block......
		XFuture<T> local = future.handle((r, t) -> {
			//r and t can BOTH be null but t is not null on failure and null on good response
			if(t == null) //response is good so return it
				return XFuture.completedFuture(r);
			
			//RUN the finally code BUT try....catch it
			try {
				return catchBlock.apply(t);
			} catch (Throwable e) { 
				//IF the catch block throws sync exception, they get screwed so just pass original exception up the chain
				//along with the suppressed exception inside that....
				t.addSuppressed(e);
				return this.<T>failedFuture(t);
			}

		}).thenCompose(Function.identity());
	
		return local;
	}
	
	/**
	 * This is just a version of catchBlock above  except this type of catch block always fails
	 * so it's not meant for recovery situations on returning a success response
	 * 
	 * USAGE:
	 * 
	 *	XFuture<Response> response = futureUtil.wrap(
	 *		() -> pageNotFoundRouter.invokeNotFoundRoute(requestCtx, responseCb),
	 *		(e) -> new RuntimeException("NotFound Route had an exception", e)
	 *	);
	 */
	public <T> XFuture<T> catchBlockWrap(
			Callable<XFuture<T>> function,
			Function<Throwable, Throwable> wrapException
	) {
		return catchBlock(function, (t) -> failedFuture(wrapException.apply(t)));
	}

	/**
	 * Copying Twitter filters, we convert all synchronous exceptions to XFuture.failedFuture
	 * as there has never been a need over all twitter's 1000's of servers over 10 years where we need
	 * to distinguish  between a synchronous exception and an asynchronous one.  IF YOU HAPPEN to find a
	 * single case, 1. create a special AsyncException and key off that and 2. PLEASE do tell me as I am 
	 * very curious if a case like that exists
	 * 
	 * USAGE:
	 * 
	 * 	XFuture<InitiationResult> future =
	 *          futureUtil.syncToAsyncException( 
	 *                  () -> http11Handler.initialData(socket, b)
	 *          );
     *
	 */
	//Change to Runnable...
	public <T> XFuture<T> syncToAsyncException(Callable<XFuture<T>> callable) {
	    try {
			//DAMN these damn InvocationTargetExceptions that just fucking wrap the original
			//GET rid of checked exceptions....in reality InvocationTargetException == FUCKING ANYTHING!!!
			//This ensures the original thrown exception comes through. 
	    	
	    	//specifically, it's important that NotFoundException is unwrapped so above layers can catch
			//that instead of this damn InvocationTargetException
	        return callable.call();
	    } catch (InvocationTargetException e) {
	    	if(e.getCause() != null)
	    		return failedFuture(e.getCause());
	    	return failedFuture(e);
	    } catch (Exception ex) {
	        return failedFuture(ex);
	    }
	}

    //In jdk8, they don't have XFuture.failedFuture yet :(
    public <T> XFuture<T> failedFuture(Throwable ex) {
        XFuture<T> future = new XFuture<>();
        future.completeExceptionally(ex);
        return future;
    }

}
