package org.webpieces.webserver.test;

import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.templating.api.TemplateCompileConfig;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.impl.DevTemplateService;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.inject.Binder;
import com.google.inject.Module;

public class PlatformOverridesForTest implements Module {
	
	private static final Logger log = LoggerFactory.getLogger(PlatformOverridesForTest.class);
	private TemplateCompileConfig templateConfig;
	
	public PlatformOverridesForTest() {
		this(new TemplateCompileConfig(isGradleRunning()));
	}
	
	public PlatformOverridesForTest(TemplateCompileConfig templateCompileConfig) {
		this.templateConfig = templateCompileConfig;
	}
	
	@Override
	public void configure(Binder binder) {
		binder.bind(HttpFrontendManager.class).toInstance(new MockHttpFrontendMgr());
                //By using the DevTemplateService, we do not need to re-run the gradle build and generate html
                //files every time we change the html code AND instead can just run the test in our IDE.
                //That said, there is a setting when this test runs in gradle that skips this step and runs the
                //production groovy *.class file that will be run in production (ie. the test run in the IDE
                //and run in gradle differ just a little :( )
		binder.bind(TemplateService.class).to(DevTemplateService.class);
		binder.bind(TemplateCompileConfig.class).toInstance(templateConfig);
	}

	/**
	 * If gradle is running, use generated groovy class files so code coverage works AND we actually
	 * test the production class files instead of regenerating them(though 99.9% of the time, they
	 * are the same).  jacoco freaks out if we do not use the class files!
	 */
	public static boolean isGradleRunning() {
		String property = System.getProperty("gradle.running");
		if("true".equals(property)) {
			log.info("gradle running.  using class files from filesystem");
			return true;
		}
		log.info("gradle NOT running.  generating groovy class files for test so test doesn't fail with class not found in IDE ever");
		return false;
	}
}
