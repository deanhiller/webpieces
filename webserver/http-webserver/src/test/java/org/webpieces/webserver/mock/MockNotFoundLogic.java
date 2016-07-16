package org.webpieces.webserver.mock;

import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.webserver.basic.biz.NotFoundLib;

public class MockNotFoundLogic extends NotFoundLib {

	private boolean throwNotFound;
	private boolean throwRuntime;

	@Override
	public void someBusinessLogic() {
		if(throwNotFound)
			throw new NotFoundException("testing if app throws NotFoundException(which they shouldn't) results in 500 page");
		else if(throwRuntime)
			throw new RuntimeException("testing throwing exception on notFound route results in 500");
	}

	public void throwNotFound() {
		throwNotFound = true;
	}

	public void throwRuntime() {
		throwRuntime = true;
	}
}
