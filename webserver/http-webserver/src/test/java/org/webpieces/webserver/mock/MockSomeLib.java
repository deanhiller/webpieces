package org.webpieces.webserver.mock;

import java.util.ArrayList;
import java.util.List;
import org.webpieces.util.futures.XFuture;

import org.webpieces.http.exception.NotFoundException;
import org.webpieces.webserver.basic.app.biz.SomeLib;
import org.webpieces.webserver.basic.app.biz.UserDto;

public class MockSomeLib extends SomeLib {

	private boolean throwNotFound;
	private boolean throwRuntime;
	private List<XFuture<Integer>> queuedFutures = new ArrayList<>();
	private UserDto lastUser;

	@Override
	public XFuture<Integer> someBusinessLogic() {
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

	public void queueFuture(XFuture<Integer> future2) {
		queuedFutures.add(future2);
	}
	
	@Override
    public void validateUser(UserDto user) {
		this.lastUser = user;
	}
	
	public UserDto getUser() {
		return lastUser;
	}
}
