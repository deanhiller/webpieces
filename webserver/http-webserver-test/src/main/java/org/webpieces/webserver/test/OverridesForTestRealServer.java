package org.webpieces.webserver.test;

import org.webpieces.templatingdev.api.DevTemplateModule;
import org.webpieces.templatingdev.api.TemplateCompileConfig;

import com.google.inject.Binder;
import com.google.inject.Module;

import io.micrometer.core.instrument.MeterRegistry;

public class OverridesForTestRealServer implements Module {
	
	private TemplateCompileConfig templateConfig;
	private MeterRegistry metrics;

	/**
	 * NEED to fix Server.java which passes in PlatformOverrides containing the metrics binding AND the constructor
	 * ALSO takes a metrics in it and then binds it in another module....this is kind of ugly and need to clean it up
	 * later
	 */
	@Deprecated
	public OverridesForTestRealServer() {
		this((MeterRegistry)null);
	}
	
	public OverridesForTestRealServer(MeterRegistry metrics) {
		this(new TemplateCompileConfig(OverridesForTest.isGradleRunning()));
		this.metrics = metrics;
	}
	
	public OverridesForTestRealServer(TemplateCompileConfig templateCompileConfig) {
		this.templateConfig = templateCompileConfig;
	}
	
	@Override
	public void configure(Binder binder) {
		if(metrics != null) //VERY VERY ugly...
			binder.bind(MeterRegistry.class).toInstance(metrics);
		
        //By using the DevTemplateService, we do not need to re-run the gradle build and generate html
        //files every time we change the html code AND instead can just run the test in our IDE.
        //That said, there is a setting when this test runs in gradle that skips this step and runs the
        //production groovy *.class file that will be run in production (ie. the test run in the IDE
        //and run in gradle differ just a little :( )
		binder.install(new DevTemplateModule(templateConfig));
	}
}
