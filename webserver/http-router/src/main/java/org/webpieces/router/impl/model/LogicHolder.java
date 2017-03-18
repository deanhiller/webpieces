package org.webpieces.router.impl.model;

import java.io.File;
import java.nio.charset.Charset;

import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.loader.ControllerLoader;

import com.google.inject.Injector;

public class LogicHolder {

	private ReverseRoutes reverseRoutes;
	private ControllerLoader finder;
	private Injector injector;
	private RouterConfig config;

	public LogicHolder(ReverseRoutes reverseRoutes, 
			ControllerLoader finder, Injector injector, RouterConfig config) {
				this.reverseRoutes = reverseRoutes;
				this.finder = finder;
				this.injector = injector;
				this.config = config;
	}

	public ReverseRoutes getReverseRoutes() {
		return reverseRoutes;
	}

	public ControllerLoader getFinder() {
		return finder;
	}

	public Injector getInjector() {
		return injector;
	}

	public Charset getUrlEncoding() {
		return config.getUrlEncoding();
	}

	public File getCachedCompressedDirectory() {
		return config.getCachedCompressedDirectory();
	}
	
	
}
