package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.exceptions.WebpiecesException;

public class ExceptionWrap {

	public static <T> CompletableFuture<T> wrapException(
			CompletableFuture<T> future, 
			Function<Throwable, Throwable> chainedException
	) {
		CompletableFuture<T> local = future.handle((r, t) -> {
			if(t != null) {
				CompletableFuture<T> failedFuture = new CompletableFuture<>();
				if(t instanceof WebpiecesException) {
					//If already known, it's important typically to skip chaining
					failedFuture.completeExceptionally(t);
				} else
					failedFuture.completeExceptionally(chainedException.apply(t));
				
				return failedFuture;
			}
			
			CompletableFuture<T> res = CompletableFuture.<T>completedFuture(r);
			return res;
		}).thenCompose(Function.identity());
	
		return local;
	}
	
}
