package org.webpieces.webserver.test;

import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.impl.DevTemplateService;

import com.google.inject.Binder;
import com.google.inject.Module;

public class PlatformOverridesForTest implements Module {
	@Override
	public void configure(Binder binder) {
		binder.bind(HttpFrontendManager.class).toInstance(new MockHttpFrontendMgr());
		//This actually helps us test the full cycle of gradle plugin compile to using
		//the compiled template where the ProdTemplateService would test 'less' code
		//so we get more bang for our buck in code coverage...
		binder.bind(TemplateService.class).to(DevTemplateService.class);
	}
}
