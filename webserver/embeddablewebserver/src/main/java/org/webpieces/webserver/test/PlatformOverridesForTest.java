package org.webpieces.webserver.test;

import org.webpieces.frontend.api.HttpFrontendManager;

import com.google.inject.Binder;
import com.google.inject.Module;

public class PlatformOverridesForTest implements Module {
	@Override
	public void configure(Binder binder) {
		binder.bind(HttpFrontendManager.class).toInstance(new MockHttpFrontendMgr());
	}
}
