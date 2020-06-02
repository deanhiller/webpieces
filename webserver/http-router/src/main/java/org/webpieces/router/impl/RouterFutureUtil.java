package org.webpieces.router.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.webpieces.http2engine.api.StreamRef;

public class RouterFutureUtil {


	/**
	 * USAGE: 
	 * 
	 *      //In this example, you may catch and recover or catch and fail like a normal
	 *      //synchronous catch block EXCEPT this is for both async and sync exceptions 
	 *      //from the function
	 * 
	 *	    CompletableFuture<Response> future = futureUtil.catchBlock(
	 *			() -> handleCompleteRequestImpl(), 
	 *			(t) -> runCatchBlock(t) //can return success if recovering OR failure if not
	 *	    );
	 */
	public <T> StreamRef catchBlock(
		Callable<CompletableFuture<T>> function,
		Function<Throwable, CompletableFuture<T>> catchBlock
	) {
//		//convert sync exceptions into async exceptions so we can re-use same exception handling logic.. 
//		CompletableFuture<T> future = syncToAsyncException(function);
//		
//		//Now, handle ANY (sync or aysnc) exceptions the same by running a finally block......
//		CompletableFuture<T> local = future.handle((r, t) -> {
//			//r and t can BOTH be null but t is not null on failure and null on good response
//			if(t == null) //response is good so return it
//				return CompletableFuture.completedFuture(r);
//			
//			//RUN the finally code BUT try....catch it
//			try {
//				return catchBlock.apply(t);
//			} catch (Throwable e) { 
//				//IF the catch block throws sync exception, they get screwed so just pass original exception up the chain
//				//along with the suppressed exception inside that....
//				t.addSuppressed(e);
//				return this.<T>failedFuture(t);
//			}
//
//		}).thenCompose(Function.identity());
//	
//		return local;
		return null;
	}
}
