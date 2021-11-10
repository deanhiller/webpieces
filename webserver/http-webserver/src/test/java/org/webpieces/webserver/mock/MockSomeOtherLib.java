package org.webpieces.webserver.mock;

import java.util.ArrayList;
import java.util.List;
import org.webpieces.util.futures.XFuture;

import org.webpieces.http.exception.NotFoundException;
import org.webpieces.webserver.basic.app.biz.SomeOtherLib;
import org.webpieces.webserver.basic.app.biz.UserDto;

public class MockSomeOtherLib extends SomeOtherLib {

	private boolean throwNotFound;
	private boolean throwRuntime;
	private List<XFuture<Integer>> queueFuture = new ArrayList<>();
	private UserDto lastUser;

	@Override
	public XFuture<Integer> someBusinessLogic() {
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

	public void queueFuture(XFuture<Integer> future) {
		this.queueFuture.add(future);
	}

	@Override
	public void saveUser(UserDto user) {
		this.lastUser = user;
	}
	
	public UserDto getUser() {
		return lastUser;
	}
	
}
