package org.webpieces.webserver.dev;

import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.webserver.test.MockHttpFrontendMgr;

import com.google.inject.Binder;
import com.google.inject.Module;

public class MockFrontEndModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(HttpFrontendManager.class).toInstance(new MockHttpFrontendMgr());
	}

}
