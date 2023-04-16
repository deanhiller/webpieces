package org.webpieces.webserver.test;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Constants;
import org.webpieces.mock.time.MockTime;
import org.webpieces.mock.time.MockTimer;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.templatingdev.api.DevTemplateModule;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.util.cmdline2.JvmEnv;
import org.webpieces.util.threading.DirectExecutorService;
import org.webpieces.util.time.Time;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;

import io.micrometer.core.instrument.MeterRegistry;

public class OverridesForEmbeddedSvrWithParsing implements Module {
	
	private static final Logger log = LoggerFactory.getLogger(OverridesForEmbeddedSvrWithParsing.class);

	private TemplateCompileConfig templateConfig;
	private MockChannelManager mgr;
	private MockTime time;
	private MockTimer mockTimer;
	private MeterRegistry metrics;
	private Map<String, String> simulatedEnv;

	public OverridesForEmbeddedSvrWithParsing(
			MockChannelManager mgr,
			MockTime time,
			MockTimer mockTimer,
			MeterRegistry metrics
	) {
		this(mgr, time, mockTimer, metrics, null);
	}

	public OverridesForEmbeddedSvrWithParsing(
			MockChannelManager mgr,
			MockTime time,
			MockTimer mockTimer,
			MeterRegistry metrics,
			Map<String, String> simulatedEnv
 	) {
		this(mgr, time, mockTimer, new TemplateCompileConfig(isGradleRunning()), metrics, simulatedEnv);
	}
	
	public OverridesForEmbeddedSvrWithParsing(
			MockChannelManager mgr,
			MockTime time,
			MockTimer mockTimer,
			TemplateCompileConfig config,
			MeterRegistry metrics,
			Map<String, String> simulatedEnv
	) {
		this.mgr = mgr;
		this.time = time;
		this.mockTimer = mockTimer;
		templateConfig = config;
		this.metrics = metrics;
		this.simulatedEnv = simulatedEnv;
	}
	
	@Override
	public void configure(Binder binder) {
		binder.bind(MeterRegistry.class).toInstance(metrics);
		binder.bind(ChannelManager.class).toInstance(mgr);
		binder.bind(Time.class).toInstance(time);
		binder.bind(ScheduledExecutorService.class).toInstance(mockTimer);

		if(simulatedEnv != null) {
			binder.bind(JvmEnv.class).toInstance(new SimulatedEnv(simulatedEnv));
		}
        //By using the DevTemplateService, we do not need to re-run the gradle build and generate html
        //files every time we change the html code AND instead can just run the test in our IDE.
        //That said, there is a setting when this test runs in gradle that skips this step and runs the
        //production groovy *.class file that will be run in production (ie. the test run in the IDE
        //and run in gradle differ just a little :( )
		//BUTTTTTT, the upside is when run in gradle we are running the full prod version
		
		binder.install(new DevTemplateModule(templateConfig));
	}

	@Provides
	@Singleton
	@Named(Constants.FILE_READ_EXECUTOR)
	public ExecutorService provideExecutor() {
		return new DirectExecutorService();
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
