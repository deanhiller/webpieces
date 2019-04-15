package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;

public class ContextWrap {

	public static CompletableFuture<Void> wrap(RequestContext ctx, Supplier<CompletableFuture<Void>> function) {
		boolean wasSet = Current.isContextSet();
		if(!wasSet)
			Current.setContext(ctx); //Allow html tags to use the contexts
		try {
			CompletableFuture<Void> future = function.get();
			return future;
		} finally {
			if(!wasSet) //then reset
				Current.setContext(null);
		}
	}
}
