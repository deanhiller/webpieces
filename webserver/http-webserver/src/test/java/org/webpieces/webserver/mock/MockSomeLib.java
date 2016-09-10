package org.webpieces.webserver.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.webserver.basic.app.biz.SomeLib;
import org.webpieces.webserver.basic.app.biz.UserDbo;

public class MockSomeLib extends SomeLib {

	private boolean throwNotFound;
	private boolean throwRuntime;
	private List<CompletableFuture<Integer>> queuedFutures = new ArrayList<>();
	private UserDbo lastUser;

	@Override
	public CompletableFuture<Integer> someBusinessLogic() {
		if(throwNotFound)
			throw new NotFoundException("testing if app throws NotFoundException(which they shouldn't) results in 500 page");
		else if(throwRuntime)
			throw new RuntimeException("testing throwing exception on notFound route results in 500");
		else if(queuedFutures.size() > 0) {
			return queuedFutures.remove(0);
		}
		return super.someBusinessLogic();
	}

	public void throwNotFound() {
		throwNotFound = true;
	}

	public void throwRuntime() {
		throwRuntime = true;
	}

	public void queueFuture(CompletableFuture<Integer> future2) {
		queuedFutures.add(future2);
	}
	
	public void validateUser(UserDbo user) {
		this.lastUser = user;
	}
	
	public UserDbo getUser() {
		return lastUser;
	}
}
