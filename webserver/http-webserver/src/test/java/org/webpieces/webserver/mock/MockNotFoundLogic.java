package org.webpieces.webserver.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.webserver.basic.biz.NotFoundLib;

public class MockNotFoundLogic extends NotFoundLib {

	private boolean throwNotFound;
	private boolean throwRuntime;
	private List<CompletableFuture<Integer>> queueFuture = new ArrayList<>();

	@Override
	public CompletableFuture<Integer> someBusinessLogic() {
		if(throwNotFound)
			throw new NotFoundException("testing if app throws NotFoundException in certain conditions");
		else if(throwRuntime)
			throw new RuntimeException("testing throwing exception results in 500");
		else if(queueFuture.size() > 0)
			return queueFuture.remove(0);
		
		return super.someBusinessLogic();
	}

	public void throwNotFound() {
		throwNotFound = true;
	}

	public void throwRuntime() {
		throwRuntime = true;
	}

	public void queueFuture(CompletableFuture<Integer> future) {
		this.queueFuture.add(future);
	}
}
