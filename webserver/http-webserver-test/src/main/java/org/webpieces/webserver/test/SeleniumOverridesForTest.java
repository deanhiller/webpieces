package org.webpieces.webserver.test;

import org.webpieces.templating.api.DevTemplateModule;
import org.webpieces.templating.api.TemplateCompileConfig;

import com.google.inject.Binder;
import com.google.inject.Module;

public class SeleniumOverridesForTest implements Module {
	
	private TemplateCompileConfig templateConfig;
	
	public SeleniumOverridesForTest() {
		this(new TemplateCompileConfig(PlatformOverridesForTest.isGradleRunning()));
	}
	
	public SeleniumOverridesForTest(TemplateCompileConfig templateCompileConfig) {
		this.templateConfig = templateCompileConfig;
	}
	
	@Override
	public void configure(Binder binder) {
                //By using the DevTemplateService, we do not need to re-run the gradle build and generate html
                //files every time we change the html code AND instead can just run the test in our IDE.
                //That said, there is a setting when this test runs in gradle that skips this step and runs the
                //production groovy *.class file that will be run in production (ie. the test run in the IDE
                //and run in gradle differ just a little :( )
		binder.install(new DevTemplateModule(templateConfig));
	}
}
