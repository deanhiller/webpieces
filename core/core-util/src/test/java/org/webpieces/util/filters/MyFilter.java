package org.webpieces.util.filters;

import java.util.concurrent.CompletableFuture;

import org.webpieces.util.futures.FutureHelper;

public class MyFilter extends Filter<Integer, String> {

	private String txt;

	public MyFilter(String txt) {
		super();
		this.txt = txt;
	}

	@Override
	public CompletableFuture<String> filter(Integer meta, Service<Integer, String> nextFilter) {
		System.out.println("txt="+txt);
		return nextFilter.invoke(meta).thenApply( s -> txt ); //translate to what we want
	}

}
